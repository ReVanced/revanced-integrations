package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.SponsorBlockUtils.formatColorString;
import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class EditTextListPreference extends ListPreference {
    private EditText mEditText;
    private int mClickedDialogEntryIndex;

    public EditTextListPreference(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            SegmentCategory category = getCategoryBySelf();

            mEditText = new EditText(builder.getContext());
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            mEditText.setText(formatColorString(category.color));
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        Color.parseColor(s.toString()); // validation
                        getDialog().setTitle(Html.fromHtml(String.format("<font color=\"%s\">⬤</font> %s", s, category.title)));
                    } catch (IllegalArgumentException ex) {
                    }
                }
            });
            builder.setView(mEditText);
            builder.setTitle(category.getTitleWithDot());

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                EditTextListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            });
            builder.setNeutralButton(str("sb_reset"), (dialog, which) -> {
                try {
                    int defaultColor = category.defaultColor;
                    category.setColor(defaultColor);
                    ReVancedUtils.showToastShort(str("sb_color_reset"));
                    getSharedPreferences().edit().putString(getColorPreferenceKey(), formatColorString(defaultColor)).apply();
                    reformatTitle();
                } catch (Exception ex) {
                    LogHelper.printException(() -> "setNeutralButton failure", ex);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);

            mClickedDialogEntryIndex = findIndexOfValue(getValue());
            builder.setSingleChoiceItems(getEntries(), mClickedDialogEntryIndex, (dialog, which) -> mClickedDialogEntryIndex = which);
        } catch (Exception ex) {
            LogHelper.printException(() -> "onPrepareDialogBuilder failure", ex);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        try {
            if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
                String value = getEntryValues()[mClickedDialogEntryIndex].toString();
                if (callChangeListener(value)) {
                    setValue(value);
                }
                String colorString = mEditText.getText().toString();
                SegmentCategory category = getCategoryBySelf();
                if (colorString.equals(formatColorString(category.color))) {
                    return;
                }
                try {
                    int color = Color.parseColor(colorString);
                    category.setColor(color);
                    getSharedPreferences().edit().putString(getColorPreferenceKey(), formatColorString(color)).apply();
                    reformatTitle();
                    ReVancedUtils.showToastShort(str("sb_color_changed"));
                } catch (IllegalArgumentException ex) {
                    ReVancedUtils.showToastShort(str("sb_color_invalid"));
                }
            }
        } catch (Exception ex) {
            LogHelper.printException(() -> "onDialogClosed failure", ex);
        }
    }

    private SegmentCategory getCategoryBySelf() {
        return SegmentCategory.byCategoryKey(getKey());
    }

    private String getColorPreferenceKey() {
        return getKey() + SponsorBlockSettings.SEGMENT_CATEGORY_COLOR_PREFERENCE_KEY_SUFFIX;
    }

    private void reformatTitle() {
        this.setTitle(getCategoryBySelf().getTitleWithDot());
    }
}