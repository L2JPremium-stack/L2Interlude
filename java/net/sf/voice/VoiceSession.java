package net.sf.voice;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class VoiceSession
{
    public enum Channel
    {
        GLOBAL,
        PARTY
    }

    private final SocketAddress address;
    private final long createdAt;
    private final Set<Integer> mutedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    private volatile int objectId;
    private volatile String name;
    private volatile Channel channel;
    private volatile boolean authenticated;
    private volatile boolean muted;
    private volatile boolean talking;
    private volatile long lastSeen;
    private volatile long lastTalk;

    private volatile int packetsThisSecond;
    private volatile long packetSecond;

    public VoiceSession(SocketAddress address)
    {
        this.address = address;
        this.createdAt = System.currentTimeMillis();
        this.lastSeen = createdAt;
        this.channel = Channel.GLOBAL;
        this.name = "Player";
    }

    public SocketAddress getAddress()
    {
        return address;
    }

    public int getObjectId()
    {
        return objectId;
    }

    public String getName()
    {
        return name;
    }

    public Channel getChannel()
    {
        return channel;
    }

    public boolean isAuthenticated()
    {
        return authenticated;
    }

    public boolean isMuted()
    {
        return muted;
    }

    public boolean isTalking()
    {
        return talking && (System.currentTimeMillis() - lastTalk) <= VoiceConfig.TALK_TIMEOUT_MS;
    }

    public void authenticate(int objectId, String name)
    {
        this.objectId = objectId;
        this.name = (name == null || name.isEmpty()) ? "Player" : name;
        this.authenticated = true;
        touch();
    }

    public void setChannel(Channel channel)
    {
        this.channel = channel == null ? Channel.GLOBAL : channel;
        touch();
    }

    public void setMuted(boolean muted)
    {
        this.muted = muted;
        if (muted)
            setTalking(false);
        touch();
    }

    public void setTalking(boolean talking)
    {
        this.talking = talking;
        if (talking)
            this.lastTalk = System.currentTimeMillis();
        touch();
    }

    public void mutePlayer(int objectId, boolean muted)
    {
        if (muted)
            mutedPlayers.add(objectId);
        else
            mutedPlayers.remove(objectId);
    }

    public boolean hasMuted(int objectId)
    {
        return mutedPlayers.contains(objectId);
    }

    public void touch()
    {
        lastSeen = System.currentTimeMillis();
    }

    public boolean isExpired(long now)
    {
        return now - lastSeen > VoiceConfig.SESSION_TIMEOUT_MS;
    }

    public boolean allowAudioPacket()
    {
        long nowSecond = System.currentTimeMillis() / 1000L;
        if (packetSecond != nowSecond)
        {
            packetSecond = nowSecond;
            packetsThisSecond = 0;
        }
        return ++packetsThisSecond <= VoiceConfig.MAX_AUDIO_PACKETS_PER_SECOND;
    }
}
