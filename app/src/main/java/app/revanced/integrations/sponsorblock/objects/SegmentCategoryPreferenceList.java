package app.revanced.integrations.sponsorblock.objects;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.ListPreference;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Objects;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public class SegmentCategoryPreferenceList extends ListPreference {
    private SegmentCategory category;
    private EditText mEditText;
    private int mClickedDialogEntryIndex;

    public SegmentCategoryPreferenceList(Context context) {
        super(context);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        try {
            category = Objects.requireNonNull(SegmentCategory.byCategoryKey(getKey()));

            Context context = builder.getContext();
            TableLayout table = new TableLayout(context);
            table.setOrientation(LinearLayout.HORIZONTAL);
            table.setPadding(70, 0, 150, 0);
            table.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TableRow row = new TableRow(context);

            TextView colorTextLabel = new TextView(context);
            colorTextLabel.setText(str("sb_color_dot_label"));
            row.addView(colorTextLabel);

            TextView colorDotView = new TextView(context);
            colorDotView.setText(category.getCategoryColorDot());
            colorDotView.setPadding(30, 0, 30, 0);
            row.addView(colorDotView);

            mEditText = new EditText(context);
            mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            mEditText.setText(category.colorString());
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
                        String colorString = s.toString();
                        if (!colorString.startsWith("#")) {
                            s.insert(0, "#"); // recursively calls back into this method
                            return;
                        }
                        if (colorString.length() > 7) {
                            s.delete(7, colorString.length());
                            return;
                        }
                        final int color = Color.parseColor(colorString);
                        colorDotView.setText(SegmentCategory.getCategoryColorDot(color));
                    } catch (IllegalArgumentException ex) {
                        // ignore
                    }
                }
            });
            mEditText.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(mEditText);

            table.addView(row);
            builder.setView(table);
            builder.setTitle(category.title.toString());

            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                onClick(dialog, DialogInterface.BUTTON_POSITIVE);
            });
            builder.setNeutralButton(str("sb_reset_color"), (dialog, which) -> {
                try {
                    SharedPreferences.Editor editor = getSharedPreferences().edit();
                    category.setColor(category.defaultColor);
                    category.save(editor);
                    editor.apply();
                    reformatTitle();
                    ReVancedUtils.showToastShort(str("sb_color_reset"));
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
                try {
                    final int color = Color.parseColor(colorString) & 0xFFFFFF;
                    if (color == category.color) {
                        return;
                    }
                    SharedPreferences.Editor editor = getSharedPreferences().edit();
                    category.setColor(color);
                    category.save(editor);
                    editor.apply();
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

    private void reformatTitle() {
        this.setTitle(category.getTitleWithColorDot());
    }
}