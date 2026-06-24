package net.sf.voice;

import java.util.concurrent.atomic.AtomicLong;

public final class VoiceMetrics
{
    public final AtomicLong rxPackets = new AtomicLong();
    public final AtomicLong txPackets = new AtomicLong();
    public final AtomicLong rxBytes = new AtomicLong();
    public final AtomicLong txBytes = new AtomicLong();
    public final AtomicLong droppedPackets = new AtomicLong();
    public final AtomicLong authOk = new AtomicLong();
    public final AtomicLong authFail = new AtomicLong();

    public String dump(int sessions, int authenticated)
    {
        return "========== Voice UDP Metrics ==========" + System.lineSeparator() +
            "Sessions:       " + sessions + System.lineSeparator() +
            "Authenticated:  " + authenticated + System.lineSeparator() +
            "RX packets:     " + rxPackets.get() + System.lineSeparator() +
            "TX packets:     " + txPackets.get() + System.lineSeparator() +
            "RX bytes:       " + rxBytes.get() + System.lineSeparator() +
            "TX bytes:       " + txBytes.get() + System.lineSeparator() +
            "Dropped:        " + droppedPackets.get() + System.lineSeparator() +
            "Auth OK:        " + authOk.get() + System.lineSeparator() +
            "Auth Fail:      " + authFail.get();
    }
}
