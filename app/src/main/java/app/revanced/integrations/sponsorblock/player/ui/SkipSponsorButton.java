package app.revanced.integrations.sponsorblock.player.ui;

import static app.revanced.integrations.sponsorblock.SponsorBlockSettings.SegmentInfo;
import static app.revanced.integrations.sponsorblock.StringRef.str;
import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdByName;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.PlayerController;
import app.revanced.integrations.utils.LogHelper;

public class SkipSponsorButton extends FrameLayout {
    private LinearLayout skipSponsorBtnContainer;
    private TextView skipSponsorTextView;
    private CharSequence skipSponsorTextCompact;
    public int defaultBottomMargin;
    public int ctaBottomMargin;
    private final Paint background;
    private final Paint border;
    private static final boolean highContrast = true;

    public SkipSponsorButton(Context context) {
        super(context);
        background = new Paint();
        border = new Paint();
        initialize(context);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        background = new Paint();
        border = new Paint();
        initialize(context);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        background = new Paint();
        border = new Paint();
        initialize(context);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);
        background = new Paint();
        border = new Paint();
        initialize(context);
    }

    private void initialize(Context context) {
        LayoutInflater.from(context).inflate(getResourceIdByName(context, "skip_sponsor_button", "layout"), this, true);  // layout:skip_ad_button
        setMinimumHeight(getResources().getDimensionPixelSize(getResourceIdByName(context, "ad_skip_ad_button_min_height", "dimen")));  // dimen:ad_skip_ad_button_min_height
        skipSponsorBtnContainer = findViewById(getResourceIdByName(context, "skip_sponsor_button_container", "id"));  // id:skip_ad_button_container
        background.setColor(context.getColor(getResourceIdByName(context, "skip_ad_button_background_color", "color")));  // color:skip_ad_button_background_color);
        background.setStyle(Paint.Style.FILL);
        border.setColor(context.getColor(getResourceIdByName(context, "skip_ad_button_border_color", "color")));  // color:skip_ad_button_border_color);
        border.setStrokeWidth(getResources().getDimension(getResourceIdByName(context, "ad_skip_ad_button_border_width", "dimen")));  // dimen:ad_skip_ad_button_border_width);
        border.setStyle(Paint.Style.STROKE);
        skipSponsorTextView = findViewById(getResourceIdByName(context, "skip_sponsor_button_text", "id"));  // id:skip_ad_button_text;
        Resources resources = context.getResources();
        defaultBottomMargin = resources.getDimensionPixelSize(getResourceIdByName(context, "skip_button_default_bottom_margin", "dimen"));  // dimen:skip_button_default_bottom_margin
        ctaBottomMargin = resources.getDimensionPixelSize(getResourceIdByName(context, "skip_button_cta_bottom_margin", "dimen"));  // dimen:skip_button_cta_bottom_margin
        skipSponsorTextCompact = str("skip_button_text_compact");  // string:skip_ads "Skip ads"

        skipSponsorBtnContainer.setOnClickListener(v -> {
            LogHelper.printDebug(() -> "Skip button clicked");
            PlayerController.onSkipSponsorClicked();
        });
    }

    @Override  // android.view.ViewGroup
    protected final void dispatchDraw(Canvas canvas) {
        final int left = skipSponsorBtnContainer.getLeft();
        final int top = skipSponsorBtnContainer.getTop();
        final int leftPlusWidth = (left + skipSponsorBtnContainer.getWidth());
        final int topPlusHeight = (top + skipSponsorBtnContainer.getHeight());
        canvas.drawRect(left, top, leftPlusWidth, topPlusHeight, background);
        if (!highContrast) {
            canvas.drawLines(new float[]{
                            leftPlusWidth, top,
                            left, top,
                            left, top,
                            left, topPlusHeight,
                            left, topPlusHeight,
                            leftPlusWidth, topPlusHeight}, border);
        }

        super.dispatchDraw(canvas);
    }

    /**
     * @return true, if this button state was changed
     */
    public boolean updateSkipButtonText(SegmentInfo info) {
        CharSequence newText = SettingsEnum.SB_USE_COMACT_SKIPBUTTON.getBoolean()
                ? skipSponsorTextCompact
                : info.skipButtonText.toString();
        if (newText.equals(skipSponsorTextView.getText())) {
            return false;
        }
        skipSponsorTextView.setText(newText);
        return true;
    }
}
