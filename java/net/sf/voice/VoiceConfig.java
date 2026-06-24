package net.sf.voice;

public final class VoiceConfig
{
    public static boolean ENABLED = true;
    public static int UDP_PORT = 9010;
    public static String HWID_SECRET = "BAN_L2JDEV_2070";

    // PCM atual: 10 ms, 48 kHz, mono, 16-bit = 960 bytes.
    public static int MAX_AUDIO_PAYLOAD = 960;
    public static int MAX_DATAGRAM_SIZE = 1500;

    // Controle de fluidez. Voz atrasada deve cair, não enfileirar infinito.
    public static int SESSION_TIMEOUT_MS = 30000;
    public static int TALK_TIMEOUT_MS = 15000;
    public static int MAX_ACTIVE_SPEAKERS_GLOBAL = 3;
    public static int MAX_ACTIVE_SPEAKERS_PARTY = 9;

    // Segurança simples contra flood por sessão.
    public static int MAX_AUDIO_PACKETS_PER_SECOND = 120;

    private VoiceConfig()
    {
    }
}
