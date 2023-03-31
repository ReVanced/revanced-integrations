package app.revanced.integrations.sponsorblock.ui;

import static app.revanced.integrations.utils.ReVancedUtils.getResourceIdByName;
import static app.revanced.integrations.utils.StringRef.str;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.sponsorblock.SegmentPlaybackController;
import app.revanced.integrations.sponsorblock.objects.SponsorSegment;
import app.revanced.integrations.utils.LogHelper;

public class SkipSponsorButton extends FrameLayout {
    private static final boolean highContrast = true;
    private final LinearLayout skipSponsorBtnContainer;
    private final TextView skipSponsorTextView;
    private final CharSequence skipSponsorTextCompact;
    private final Paint background;
    private final Paint border;
    final int defaultBottomMargin;
    final int ctaBottomMargin;

    public SkipSponsorButton(Context context) {
        this(context, null);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        this(context, attributeSet, defStyleAttr, 0);
    }

    public SkipSponsorButton(Context context, AttributeSet attributeSet, int defStyleAttr, int defStyleRes) {
        super(context, attributeSet, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(getResourceIdByName(context, "skip_sponsor_button", "layout"), this, true);  // layout:skip_ad_button
        setMinimumHeight(getResources().getDimensionPixelSize(getResourceIdByName(context, "ad_skip_ad_button_min_height", "dimen")));  // dimen:ad_skip_ad_button_min_height
        skipSponsorBtnContainer = Objects.requireNonNull((LinearLayout) findViewById(getResourceIdByName(context, "sb_skip_sponsor_button_container", "id")));  // id:skip_ad_button_container
        background = new Paint();
        background.setColor(context.getColor(getResourceIdByName(context, "skip_ad_button_background_color", "color")));  // color:skip_ad_button_background_color);
        background.setStyle(Paint.Style.FILL);
        border = new Paint();
        border.setColor(context.getColor(getResourceIdByName(context, "skip_ad_button_border_color", "color")));  // color:skip_ad_button_border_color);
        border.setStrokeWidth(getResources().getDimension(getResourceIdByName(context, "ad_skip_ad_button_border_width", "dimen")));  // dimen:ad_skip_ad_button_border_width);
        border.setStyle(Paint.Style.STROKE);
        skipSponsorTextView = Objects.requireNonNull((TextView) findViewById(getResourceIdByName(context, "sb_skip_sponsor_button_text", "id")));  // id:skip_ad_button_text;
        Resources resources = context.getResources();
        defaultBottomMargin = resources.getDimensionPixelSize(getResourceIdByName(context, "skip_button_default_bottom_margin", "dimen"));  // dimen:skip_button_default_bottom_margin
        ctaBottomMargin = resources.getDimensionPixelSize(getResourceIdByName(context, "skip_button_cta_bottom_margin", "dimen"));  // dimen:skip_button_cta_bottom_margin
        skipSponsorTextCompact = str("sb_skip_button_compact");  // string:skip_ads "Skip ads"

        skipSponsorBtnContainer.setOnClickListener(v -> {
            LogHelper.printDebug(() -> "Skip button clicked");
            SegmentPlaybackController.onSkipSponsorClicked();
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
                            leftPlusWidth, top, left, top,
                            left, top, left, topPlusHeight,
                            left, topPlusHeight, leftPlusWidth, topPlusHeight},
                    border);
        }

        super.dispatchDraw(canvas);
    }

    /**
     * @return true, if this button state was changed
     */
    public boolean updateSkipButtonText(SponsorSegment segment) {
        CharSequence newText = SettingsEnum.SB_USE_COMPACT_SKIPBUTTON.getBoolean()
                ? skipSponsorTextCompact
                : segment.getSkipButtonText();
        if (newText.equals(skipSponsorTextView.getText())) {
            return false;
        }
        skipSponsorTextView.setText(newText);
        return true;
    }
}
