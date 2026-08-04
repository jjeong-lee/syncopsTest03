package kr.ac.knue.fpe.common;

public final class SessionContext {
    private static final ThreadLocal<SessionUser> CURRENT = new ThreadLocal<>();
    private SessionContext() {}
    public static void set(SessionUser user) { CURRENT.set(user); }
    public static SessionUser current() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}
