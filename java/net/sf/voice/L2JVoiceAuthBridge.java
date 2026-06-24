package net.sf.voice;

import java.util.Locale;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;

 
public final class L2JVoiceAuthBridge implements VoiceAuthBridge
{
    private static final Logger LOGGER = Logger.getLogger(L2JVoiceAuthBridge.class.getName());

    @Override
    public AuthResult authenticateByHwid(String hwidPayload)
    {

        final HwidPayload payload = HwidPayload.parse(hwidPayload);
        if (payload == null)
            return AuthResult.fail("bad_payload");

        if (!VoiceConfig.HWID_SECRET.equals(payload.secret))
        {
            LOGGER.warning("Voice auth rejected: invalid HWID secret. hwid=" + payload.masked());
            return AuthResult.fail("bad_secret");
        }
        
        final Player player = findOnlinePlayerByHwid(payload);
        if (player == null)
        {
            // O cliente de voz subiu antes do character estar no mundo. O C++ já tenta novamente.
            return AuthResult.wait("player_not_online");
        }

        if (player.getClient() == null || player.getClient().isDetached())
        {
            return AuthResult.fail("client_detached");
        }
       
        return AuthResult.ok(player.getObjectId(), player.getName());
    }
    
    private static Player findOnlinePlayerByHwid(HwidPayload payload)
    {
        for (Player player : L2World.getInstance().getPlayers())
        {
            if (player == null)
                continue;

            final String serverHwid = player.getHWID();
            if (serverHwid == null || serverHwid.isEmpty())
                continue;

            if (safeEquals(serverHwid, payload.raw))
                return player;

            HwidPayload serverPayload = HwidPayload.parse(serverHwid);
            if (serverPayload != null && payload.sameMachine(serverPayload))
                return player;

            if (safeEquals(normalize(serverHwid), payload.machineKey()))
                return player;

            // aqui resolve seu caso atual
            if (safeEquals(serverHwid, payload.mac))
                return player;
        }
        return null;
    }

    private static boolean safeEquals(String a, String b)
    {
        return normalize(a).equals(normalize(b));
    }

    private static String normalize(String value)
    {
        if (value == null)
            return "";
        return value.trim().replace(" ", "").toUpperCase(Locale.ROOT);
    }

    private static final class HwidPayload
    {
        private final String raw;
        private final String cpu;
        private final String hdd;
        private final String mac;
        private final String secret;

        private HwidPayload(String raw, String cpu, String hdd, String mac, String secret)
        {
            this.raw = raw;
            this.cpu = normalize(cpu);
            this.hdd = normalize(hdd);
            this.mac = normalize(mac);
            this.secret = secret == null ? "" : secret.trim();
        }

        private static HwidPayload parse(String value)
        {
            if (value == null)
                return null;

            String[] parts = value.trim().split("\\|");
            if (parts.length < 3)
                return null;

            String secret = parts.length >= 4 ? parts[3] : "";
            if (parts[0].isEmpty() || parts[1].isEmpty() || parts[2].isEmpty())
                return null;

            return new HwidPayload(value.trim(), parts[0], parts[1], parts[2], secret);
        }

        private boolean sameMachine(HwidPayload other)
        {
            return other != null && cpu.equals(other.cpu) && hdd.equals(other.hdd) && mac.equals(other.mac);
        }

        private String machineKey()
        {
            return cpu + "|" + hdd + "|" + mac;
        }

        private String masked()
        {
            return cpu + "|" + hdd + "|" + (mac.length() > 4 ? mac.substring(0, 4) + "****" : "****");
        }
    }
}
