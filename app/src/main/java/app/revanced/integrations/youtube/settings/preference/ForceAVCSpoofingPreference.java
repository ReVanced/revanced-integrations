package app.revanced.integrations.youtube.settings.preference;

import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.patches.spoof.DeviceHardwareSupport.DEVICE_HAS_HARDWARE_DECODING_VP9;

import android.content.Context;
import android.os.Build;
import android.preference.SwitchPreference;
import android.text.Html;
import android.util.AttributeSet;

import androidx.annotation.RequiresApi;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.patches.spoof.ClientType;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings({"unused", "deprecation"})
@RequiresApi(api = Build.VERSION_CODES.O)
public class ForceAVCSpoofingPreference extends SwitchPreference {
    {
        if (!DEVICE_HAS_HARDWARE_DECODING_VP9) {
            setSummaryOn((Html.fromHtml(str("revanced_spoof_streaming_data_ios_force_avc_no_hardware_vp9_summary_on"))));
            setSummaryOff((Html.fromHtml(str("revanced_spoof_streaming_data_ios_force_avc_no_hardware_vp9_summary_off"))));
        }
    }

    public ForceAVCSpoofingPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ForceAVCSpoofingPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ForceAVCSpoofingPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ForceAVCSpoofingPreference(Context context) {
        super(context);
    }

    private void updateUI() {
        try {
            if (DEVICE_HAS_HARDWARE_DECODING_VP9) {
                return;
            }

            // Temporarily remove the preference key to allow changing this preference without
            // causing the settings UI listeners from showing reboot dialogs dialogs by the changes made here.
            String key = getKey();
            setKey(null);

            // This setting cannot be changed by the user.
            super.setEnabled(false);

            final boolean isIOS = Settings.SPOOF_STREAMING_DATA_TYPE.get() == ClientType.IOS;
            Settings.SPOOF_STREAMING_DATA_IOS_FORCE_AVC.save(isIOS);
            super.setChecked(isIOS);

            setKey(key);
        } catch (Exception ex) {
            Logger.printException(() -> "updateUI failure", ex);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            super.setEnabled(enabled);

            updateUI();
        }
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked() != checked) {
            super.setChecked(checked);

            updateUI();
        }
    }
}
