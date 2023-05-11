package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class ResettableEditTextPreference extends EditTextPreference {

    public ResettableEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public ResettableEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ResettableEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ResettableEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        SettingsEnum setting = SettingsEnum.settingFromPath(getKey());
        if (setting != null) {
            builder.setNeutralButton(str("revanced_settings_reset"), null);
        }
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        // Override the button click listener to prevent dismissing the dialog.
        Button button = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEUTRAL);
        if (button == null) {
            return;
        }
        button.setOnClickListener(v -> {
            try {
                SettingsEnum setting = Objects.requireNonNull(SettingsEnum.settingFromPath(getKey()));
                String defaultStringValue = setting.defaultValue.toString();
                EditText editText = getEditText();
                editText.setText(defaultStringValue);
                editText.setSelection(defaultStringValue.length()); // move cursor to end of text
            } catch (Exception ex) {
                LogHelper.printException(() -> "reset failure", ex);
            }
        });
    }
}
