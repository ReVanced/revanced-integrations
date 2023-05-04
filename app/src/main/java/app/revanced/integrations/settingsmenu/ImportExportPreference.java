package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.utils.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class ImportExportPreference extends EditTextPreference implements Preference.OnPreferenceClickListener {

    private String existingSettings;

    private void init() {
        setSelectable(true);

        EditText editText = getEditText();
        editText.setTextIsSelectable(true);
        editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PT, 7); // Use a smaller font to reduce text wrap.

        setOnPreferenceClickListener(this);
    }

    public ImportExportPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
    public ImportExportPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public ImportExportPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ImportExportPreference(Context context) {
        super(context);
        init();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        try {
            existingSettings = SettingsEnum.exportJSON();
            // Must set text before preparing dialog, otherwise text is non selectable if this preference is later reopened.
            getEditText().setText(existingSettings);

            // If user has a SponsorBlock user id then show a warning.
            if (SponsorBlockSettings.userHasSBPrivateId() && !SettingsEnum.SB_HIDE_EXPORT_WARNING.getBoolean()) {
                showDoNotShareSponsorBlockUserIdDialog();
                return true;
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "showDialog failure", ex);
        }
        return true;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            // Show the user the settings in JSON format.
            builder.setNeutralButton(str("revanced_settings_import_copy"), (dialog, which) -> {
                ReVancedUtils.setClipboard(getEditText().getText().toString());
            }).setPositiveButton(str("revanced_settings_import"), (dialog, which) -> {
                String replacementSettings = getEditText().getText().toString();
                if (replacementSettings.equals(existingSettings)) {
                    return;
                }
                String sbUserIdPath = SettingsEnum.SB_UUID.path;
                // If user is deleting (and not replacing) their SB user id, then verify that's what they want to do.
                if (existingSettings.contains(sbUserIdPath) && !replacementSettings.contains(sbUserIdPath)) {
                    confirmUserWantsToDeletedSBUserId(replacementSettings);
                } else {
                    importSettings(replacementSettings);
                }
            });
        } catch (Exception ex) {
            LogHelper.printException(() -> "onPrepareDialogBuilder failure", ex);
        }
    }

    private void showDoNotShareSponsorBlockUserIdDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(str("revanced_settings_import_sb_userid_present"))
                .setNeutralButton(str("revanced_settings_import_sb_userid_present_dismiss"),
                        (dialog, which) -> SettingsEnum.SB_HIDE_EXPORT_WARNING.saveValue(true))
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .show();
    }

    private void confirmUserWantsToDeletedSBUserId(String replacementSettings) {
        new AlertDialog.Builder(getContext())
                .setMessage(str("revanced_settings_import_sb_userid_removed"))
                .setNegativeButton(android.R.string.cancel, (dialog, which) ->
                        ReVancedUtils.showToastLong(str("revanced_settings_import_sb_userid_removed_canceled")))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> importSettings(replacementSettings))
                .setCancelable(false)
                .show();
    }

    private void importSettings(String replacementSettings) {
        // Import JSON settings
        try {
            ReVancedSettingsFragment.settingImportInProgress = true;
            final boolean rebootNeeded = SettingsEnum.importJSON(replacementSettings);
            if (rebootNeeded) {
                ReVancedSettingsFragment.showRebootDialog(getContext());
            }
        } finally {
            ReVancedSettingsFragment.settingImportInProgress = false;
        }
    }

}