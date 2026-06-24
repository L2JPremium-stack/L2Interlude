package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;
import net.sf.l2j.protection.hwid.HwidManager;

public final class ProtocolVersion extends L2GameClientPacket
{
	private static final Logger LOGGER = Logger.getLogger(ProtocolVersion.class.getName());
	private static final int MAX_EXTRA_DATA_LENGTH = 1024;
	private static final int MAX_HWID_FIELD_LENGTH = 64;
	private static final byte[] HWID_MAGIC =
	{
		'B', 'H', 'W', 'D'
	};

	private int _version;
	private byte[] _extraData;
	private boolean _malformed;

	@Override
	protected void readImpl()
	{
		if (_buf.remaining() < Integer.BYTES)
		{
			_malformed = true;
			return;
		}

		_version = readD();

		if (_buf.remaining() > MAX_EXTRA_DATA_LENGTH)
		{
			_malformed = true;
			return;
		}

		if (_buf.hasRemaining())
		{
			_extraData = new byte[_buf.remaining()];
			readB(_extraData);
		}
	}

	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();

		if (_malformed || client.isHwidAuthed() || !isSupportedProtocol(_version))
		{
			reject(client, "unsupported or malformed protocol " + _version);
			return;
		}

		final String payload = extractPayloadFromExtra(_extraData);
		if (payload == null)
		{
			reject(client, "missing HWID payload");
			return;
		}

		final String[] parts = payload.split("\\|", -1);
		if (parts.length != 4 || !isValidField(parts[0]) || !isValidField(parts[1]) || !isValidField(parts[2]) || !isValidField(parts[3]))
		{
			reject(client, "invalid HWID payload");
			return;
		}

		final String cpu = parts[0];
		final String hdd = parts[1];
		final String mac = parts[2];
		final String key = parts[3];

		if (!HwidManager.getInstance().validateClient(client, hdd, mac, cpu, key))
		{
			reject(client, "HWID validation failed");
			return;
		}

		client.setHwidAuthed(true);
		client.sendPacket(new KeyPacket(client.enableCrypt()));
	}

	private static boolean isSupportedProtocol(int version)
	{
		if (version < Config.MIN_PROTOCOL_REVISION || version > Config.MAX_PROTOCOL_REVISION)
			return false;

		switch (version)
		{
			case 737:
			case 740:
			case 744:
			case 746:
				return true;
			default:
				return false;
		}
	}

	private static boolean isValidField(String value)
	{
		return value != null && !value.isBlank() && value.length() <= MAX_HWID_FIELD_LENGTH;
	}

	private static void reject(L2GameClient client, String reason)
	{
		if (Config.PACKET_HANDLER_DEBUG)
			LOGGER.warning("Client " + client + " rejected during ProtocolVersion: " + reason + ".");

		client.closeNow();
	}

	private static String extractPayloadFromExtra(byte[] extra)
	{
		if (extra == null || extra.length == 0)
			return null;

		final int start = indexOf(extra, HWID_MAGIC);
		if (start < 0)
			return null;

		final int lenPos = start + HWID_MAGIC.length;
		if (extra.length - lenPos < Integer.BYTES)
			return null;

		final ByteBuffer lenBuffer = ByteBuffer.wrap(extra, lenPos, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
		final int payloadLen = lenBuffer.getInt();
		final int payloadStart = lenPos + Integer.BYTES;

		if (payloadLen <= 0 || payloadLen > extra.length - payloadStart)
			return null;

		int realLen = payloadLen;
		if (extra[payloadStart + payloadLen - 1] == 0)
			realLen--;

		if (realLen <= 0)
			return null;

		return new String(extra, payloadStart, realLen, StandardCharsets.US_ASCII).trim();
	}

	private static int indexOf(byte[] data, byte[] pattern)
	{
		if (data == null || pattern == null || pattern.length == 0 || data.length < pattern.length)
			return -1;

		outer:
		for (int i = 0; i <= data.length - pattern.length; i++)
		{
			for (int j = 0; j < pattern.length; j++)
			{
				if (data[i + j] != pattern[j])
					continue outer;
			}
			return i;
		}
		return -1;
	}
}
