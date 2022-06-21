package app.revanced.integrations.settings;

public enum PlayerType {

    NONE,
    HIDDEN,
    WATCH_WHILE_MINIMIZED,
    WATCH_WHILE_MAXIMIZED,
    WATCH_WHILE_FULLSCREEN,
    WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN,
    WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED,
    WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED,
    WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED,
    INLINE_MINIMAL,
    VIRTUAL_REALITY_FULLSCREEN,
    WATCH_WHILE_PICTURE_IN_PICTURE;

    /* renamed from: a */
    public final boolean m33524a() {
        return !m33520e() && m33523b() && m33517h();
    }

    /* renamed from: b */
    public final boolean m33523b() {
        return this == WATCH_WHILE_FULLSCREEN || this == VIRTUAL_REALITY_FULLSCREEN || this == WATCH_WHILE_PICTURE_IN_PICTURE;
    }

    /* renamed from: c */
    public final boolean m33522c() {
        return this == NONE || m33521d();
    }

    /* renamed from: d */
    public final boolean m33521d() {
        return this == INLINE_MINIMAL;
    }

    /* renamed from: e */
    public final boolean m33520e() {
        return this == WATCH_WHILE_PICTURE_IN_PICTURE;
    }

    /* renamed from: f */
    public final boolean m33519f() {
        return (this == NONE || this == HIDDEN) ? false : true;
    }

    /* renamed from: g */
    public final boolean m33518g() {
        return this == VIRTUAL_REALITY_FULLSCREEN;
    }

    /* renamed from: h */
    public final boolean m33517h() {
        return this == WATCH_WHILE_MINIMIZED || this == WATCH_WHILE_MAXIMIZED || this == WATCH_WHILE_FULLSCREEN || this == WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN || this == WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED || this == WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED || this == WATCH_WHILE_PICTURE_IN_PICTURE;
    }

    /* renamed from: i */
    public final boolean m33516i() {
        return this == WATCH_WHILE_MAXIMIZED || this == WATCH_WHILE_FULLSCREEN;
    }

    /* renamed from: j */
    public final boolean m33515j() {
        return m33516i() || this == WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN;
    }

    /* renamed from: k */
    public final boolean m33514k() {
        return this == WATCH_WHILE_MINIMIZED || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED;
    }

    /* renamed from: l */
    public final boolean m33513l() {
        return m33514k() || m33512m();
    }

    /* renamed from: m */
    public final boolean m33512m() {
        return this == WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED || this == WATCH_WHILE_SLIDING_MINIMIZED_DISMISSED || this == WATCH_WHILE_SLIDING_MAXIMIZED_FULLSCREEN || this == WATCH_WHILE_SLIDING_FULLSCREEN_DISMISSED;
    }

}
