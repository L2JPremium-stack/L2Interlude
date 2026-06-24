package net.sf.voice;

public interface VoiceAuthBridge
{
    AuthResult authenticateByHwid(String hwidPayload);

    final class AuthResult
    {
        public final boolean success;
        public final int objectId;
        public final String name;
        public final boolean wait;
        public final String reason;

        private AuthResult(boolean success, int objectId, String name, boolean wait, String reason)
        {
            this.success = success;
            this.objectId = objectId;
            this.name = name;
            this.wait = wait;
            this.reason = reason;
        }

        public static AuthResult ok(int objectId, String name)
        {
            return new AuthResult(true, objectId, name, false, "ok");
        }

        public static AuthResult fail()
        {
            return new AuthResult(false, 0, null, false, "fail");
        }
        public static AuthResult fail(String reason)
        {
            return new AuthResult(false, 0, null, false, reason);
        }

        public static AuthResult wait(String reason)
        {
            return new AuthResult(false, 0, null, true, reason);
        }
    }
}
