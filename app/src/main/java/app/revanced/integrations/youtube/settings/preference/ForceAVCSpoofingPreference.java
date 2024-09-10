package app.revanced.integrations.youtube.settings.preference;

import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static app.revanced.integrations.shared.StringRef.str;
import static app.revanced.integrations.youtube.patches.spoof.DeviceHardwareSupport.DEVICE_HAS_HARDWARE_DECODING_VP9;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.preference.SwitchPreference;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Checkable;
import android.widget.Switch;

import androidx.annotation.RequiresApi;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.integrations.youtube.patches.spoof.ClientType;
import app.revanced.integrations.youtube.patches.spoof.DeviceHardwareSupport;
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

    @Override
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    protected void onBindView(View uiView) {
        View switchView = Utils.getChildView((ViewGroup) uiView, true,
                view -> view instanceof Checkable);
        if (switchView != null) {
            Utils.hideViewByRemovingFromParentUnderCondition(true, switchView);
        }

        super.onBindView(uiView);
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

            // Always enabled, since this now only shows text descriptions.
            super.setEnabled(true);

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
        super.setEnabled(enabled);

        updateUI();
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked() != checked) {
            super.setChecked(checked);

            updateUI();
        }
    }
}
