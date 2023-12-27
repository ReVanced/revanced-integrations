package app.revanced.integrations.shared.settings;

import static app.revanced.integrations.shared.settings.Setting.ReturnType.BOOLEAN;
import static app.revanced.integrations.shared.settings.Setting.getParents;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Settings {
    public static final Setting DEBUG = new Setting("revanced_debug", BOOLEAN, FALSE);
    public static final Setting DEBUG_STACKTRACE = new Setting("revanced_debug_stacktrace", BOOLEAN, FALSE, getParents(DEBUG));
    public static final Setting DEBUG_TOAST_ON_ERROR = new Setting("revanced_debug_toast_on_error", BOOLEAN, TRUE, "revanced_debug_toast_on_error_user_dialog_message");
}
