package net.sf.voice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;

public final class VoiceUdpServer implements Runnable
{
    private static final Logger LOGGER = Logger.getLogger(VoiceUdpServer.class.getName());

    private final Map<SocketAddress, VoiceSession> sessions = new ConcurrentHashMap<>();
    private final VoiceMetrics metrics = new VoiceMetrics();
    private final VoiceAuthBridge authBridge;
    private final ExecutorService senderPool;

    private volatile boolean running;
    private DatagramSocket socket;
    private Thread thread;

    public VoiceUdpServer(VoiceAuthBridge authBridge)
    {
        this.authBridge = authBridge;
        this.senderPool = Executors.newFixedThreadPool(2, new NamedFactory("VoiceSender"));
    }
    public VoiceUdpServer()
    {
        this(new L2JVoiceAuthBridge());
    }

    public static VoiceUdpServer getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {
        protected static final VoiceUdpServer INSTANCE = new VoiceUdpServer();
    }

    public synchronized void start()
    {
        if (running || !VoiceConfig.ENABLED)
            return;

        try
        {
            socket = new DatagramSocket(VoiceConfig.UDP_PORT);
            socket.setReceiveBufferSize(1024 * 1024);
            socket.setSendBufferSize(1024 * 1024);
            running = true;
            thread = new Thread(this, "VoiceUdpServer");
            thread.setDaemon(true);
            thread.start();
            LOGGER.info("VoiceUdpServer: listening on UDP " + VoiceConfig.UDP_PORT);
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "VoiceUdpServer: could not bind UDP " + VoiceConfig.UDP_PORT, e);
        }
    }

    public synchronized void stop()
    {
        running = false;
        if (socket != null)
            socket.close();
        senderPool.shutdownNow();
    }

    @Override
    public void run()
    {
        byte[] buffer = new byte[VoiceConfig.MAX_DATAGRAM_SIZE];
        long lastCleanup = System.currentTimeMillis();

        while (running)
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                metrics.rxPackets.incrementAndGet();
                metrics.rxBytes.addAndGet(packet.getLength());

                handle(packet.getSocketAddress(), packet.getData(), packet.getOffset(), packet.getLength());

                long now = System.currentTimeMillis();
                if (now - lastCleanup > 5000L)
                {
                    cleanup(now);
                    lastCleanup = now;
                }
            }
            catch (IOException e)
            {
                if (running)
                    LOGGER.log(Level.WARNING, "VoiceUdpServer receive error", e);
            }
            catch (Exception e)
            {
                LOGGER.log(Level.WARNING, "VoiceUdpServer packet error", e);
            }
        }
    }

    private void handle(SocketAddress address, byte[] data, int offset, int length)
    {
        VoiceSession session = sessions.computeIfAbsent(address, VoiceSession::new);
        session.touch();

        String text = tryReadText(data, offset, length);
        if (text != null)
        {
            if (handleText(session, text))
                return;
        }

        byte[] pcmPrefix = "VOICE_PCM:".getBytes(StandardCharsets.US_ASCII);
        if (startsWith(data, offset, length, pcmPrefix))
        {
            int audioOffset = offset + pcmPrefix.length;
            int audioLength = length - pcmPrefix.length;
            handleAudio(session, data, audioOffset, audioLength);
        }
    }

    private boolean handleText(VoiceSession session, String text)
    {
        if ("VOICE_PING".equals(text))
        {
            sendText(session.getAddress(), "VOICE_OK");
            return true;
        }

        if (text.startsWith("VOICE_AUTH_HWID:"))
        {
            VoiceAuthBridge.AuthResult result = authBridge.authenticateByHwid(text.substring(16));
            if (result != null && result.success)
            {
                session.authenticate(result.objectId, result.name);
                metrics.authOk.incrementAndGet();
                sendText(session.getAddress(), "VOICE_AUTH_OK:" + result.objectId + ":" + result.name);
                LOGGER.info("Voice auth OK: " + result.name + " objectId=" + result.objectId + " addr=" + session.getAddress());

            }
            else if (result != null && result.wait)
            {
                sendText(session.getAddress(), "VOICE_AUTH_WAIT");
            }
            else
            {
                metrics.authFail.incrementAndGet();
                LOGGER.warning("Voice auth FAIL from " + session.getAddress() + " payload=" + text);
                sendText(session.getAddress(), "VOICE_AUTH_FAIL");
            }
            return true;
        }

        if (!session.isAuthenticated())
        {
            sendText(session.getAddress(), "VOICE_NOT_AUTH");
            return true;
        }

        if ("VOICE_CHANNEL:GLOBAL".equals(text))
        {
            session.setChannel(VoiceSession.Channel.GLOBAL);
            sendText(session.getAddress(), "VOICE_CHANNEL_OK:GLOBAL");
            return true;
        }

        if ("VOICE_CHANNEL:PARTY".equals(text))
        {
            session.setChannel(VoiceSession.Channel.PARTY);
            sendText(session.getAddress(), "VOICE_CHANNEL_OK:PARTY");
            return true;
        }

        if ("VOICE_TALK_START".equals(text))
        {
            session.setTalking(true);
            broadcastTextToListeners(session, "VOICE_USER_TALK_START:" + session.getObjectId() + ":" + session.getName());
            return true;
        }

        if ("VOICE_TALK_STOP".equals(text))
        {
            session.setTalking(false);
            broadcastTextToListeners(session, "VOICE_USER_TALK_STOP:" + session.getObjectId());
            return true;
        }

        if ("VOICE_MUTE".equals(text))
        {
            session.setMuted(true);
            return true;
        }

        if ("VOICE_UNMUTE".equals(text))
        {
            session.setMuted(false);
            return true;
        }

        if (text.startsWith("VOICE_MUTE_PLAYER:"))
        {
            session.mutePlayer(parseInt(text.substring(18)), true);
            return true;
        }

        if (text.startsWith("VOICE_UNMUTE_PLAYER:"))
        {
            session.mutePlayer(parseInt(text.substring(20)), false);
            return true;
        }

        return false;
    }

    private void handleAudio(VoiceSession speaker, byte[] data, int audioOffset, int audioLength)
    {
        if (!speaker.isAuthenticated() || speaker.isMuted() || !speaker.isTalking())
        {
            metrics.droppedPackets.incrementAndGet();
            return;
        }

        if (audioLength <= 0 || audioLength > VoiceConfig.MAX_AUDIO_PAYLOAD || !speaker.allowAudioPacket())
        {
            metrics.droppedPackets.incrementAndGet();
            return;
        }

        List<VoiceSession> listeners = getListeners(speaker);
        if (listeners.isEmpty())
            return;

        byte[] prefix = ("VOICE_PCM_FROM:" + speaker.getObjectId() + ":").getBytes(StandardCharsets.US_ASCII);
        byte[] out = new byte[prefix.length + audioLength];
        System.arraycopy(prefix, 0, out, 0, prefix.length);
        System.arraycopy(data, audioOffset, out, prefix.length, audioLength);

        for (VoiceSession listener : listeners)
            sendBytes(listener.getAddress(), out);
    }

    private List<VoiceSession> getListeners(VoiceSession speaker)
    {
        List<VoiceSession> result = new ArrayList<>();
        Collection<VoiceSession> all = sessions.values();

        for (VoiceSession other : all)
        {
            if (other == speaker || !other.isAuthenticated() || other.isMuted())
                continue;
            if (other.hasMuted(speaker.getObjectId()))
                continue;
            if (other.getChannel() != speaker.getChannel())
                continue;

            if (!canListen(speaker, other))
                continue;

            result.add(other);
        }
        return result;
    }

    private static boolean canListen(VoiceSession speaker, VoiceSession listener)
    {
        if (speaker.getChannel() == VoiceSession.Channel.PARTY)
        {
            final Player sp = L2World.getInstance().getPlayer(speaker.getObjectId());
            final Player li = L2World.getInstance().getPlayer(listener.getObjectId());

            if (sp == null || li == null || !sp.isInParty() || !li.isInParty())
                return false;

            return sp.getParty() == li.getParty();
        }

        // GLOBAL atual: todos do canal global escutam.
        // Depois podemos trocar por região/clan/command/evento.
        return true;
    }

    private void broadcastTextToListeners(VoiceSession speaker, String text)
    {
        for (VoiceSession listener : getListeners(speaker))
            sendText(listener.getAddress(), text);
    }

    private void sendText(SocketAddress address, String text)
    {
        sendBytes(address, text.getBytes(StandardCharsets.US_ASCII));
    }

    private void sendBytes(SocketAddress address, byte[] data)
    {
        senderPool.execute(() ->
        {
            try
            {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                packet.setSocketAddress(address);
                socket.send(packet);
                metrics.txPackets.incrementAndGet();
                metrics.txBytes.addAndGet(data.length);
            }
            catch (Exception e)
            {
                metrics.droppedPackets.incrementAndGet();
            }
        });
    }

    private void cleanup(long now)
    {
        sessions.values().removeIf(s -> s.isExpired(now));
    }

    public String dumpMetrics()
    {
        int authenticated = 0;
        for (VoiceSession session : sessions.values())
            if (session.isAuthenticated())
                authenticated++;
        return metrics.dump(sessions.size(), authenticated);
    }

    private static String tryReadText(byte[] data, int offset, int length)
    {
        int max = Math.min(length, 1024);
        for (int i = 0; i < max; i++)
        {
            int b = data[offset + i] & 0xFF;
            if (b == 0 || b < 9 || b > 126)
                return null;
        }
        return new String(data, offset, max, StandardCharsets.US_ASCII);
    }

    private static boolean startsWith(byte[] data, int offset, int length, byte[] prefix)
    {
        if (length < prefix.length)
            return false;
        for (int i = 0; i < prefix.length; i++)
            if (data[offset + i] != prefix[i])
                return false;
        return true;
    }

    private static int parseInt(String value)
    {
        try
        {
            return Integer.parseInt(value.trim());
        }
        catch (Exception e)
        {
            return 0;
        }
    }

    private static final class NamedFactory implements ThreadFactory
    {
        private final String name;
        private int id;

        private NamedFactory(String name)
        {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, name + "-" + (++id));
            t.setDaemon(true);
            return t;
        }
    }
}
