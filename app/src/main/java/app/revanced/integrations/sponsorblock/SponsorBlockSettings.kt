package app.revanced.integrations.sponsorblock

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.text.Html
import android.text.TextUtils
import app.revanced.integrations.settings.SettingsEnum
import app.revanced.integrations.utils.SharedPrefHelper
import app.revanced.integrations.utils.SharedPrefHelper.SharedPrefNames
import app.revanced.integrations.utils.StringRef
import app.revanced.integrations.utils.StringRef.sf
import java.util.UUID

object SponsorBlockSettings {
    const val CATEGORY_COLOR_SUFFIX = "_color"
    val DEFAULT_BEHAVIOUR = SegmentBehaviour.IGNORE

    @JvmStatic
    var urlCategories = "[]"

    @JvmStatic
    fun update(ctx: Context) {
        if (!SettingsEnum.SB_ENABLED.boolean) {
            SkipSegmentView.hide()
            NewSegmentHelperLayout.hide()
            SponsorBlockUtils.hideShieldButton()
            SponsorBlockUtils.hideVoteButton()
            PlayerController.sponsorSegmentsOfCurrentVideo = null
            return
        }

        if (SettingsEnum.SB_NEW_SEGMENT_ENABLED.boolean) {
            NewSegmentHelperLayout.hide()
            SponsorBlockUtils.hideShieldButton()
        } else {
            SponsorBlockUtils.showShieldButton()
        }

        if (SettingsEnum.SB_VOTING_ENABLED.boolean) {
            SponsorBlockUtils.hideVoteButton()
        } else {
            SponsorBlockUtils.showVoteButton()
        }

        val prefs = SharedPrefHelper.getPreferences(ctx, SharedPrefNames.SPONSOR_BLOCK)
        val enabledCategories = mutableListOf<String>()
        for (sm in SegmentInfo.values()) {
            val color = prefs.getString(
                sm.key + CATEGORY_COLOR_SUFFIX,
                SponsorBlockUtils.formatColorString(sm.defaultColor)
            )
            sm.color = Color.parseColor(color!!)

            val behaviour = prefs.findBehaviour(sm.key) ?: sm.behaviour
            sm.behaviour = behaviour

            if (behaviour.showOnTimeBar && !sm.unsubmitted()) {
                enabledCategories.add(sm.key)
            }
        }

        urlCategories = if (enabledCategories.isNotEmpty()) {
            "[%22${TextUtils.join("%22,%22", enabledCategories)}%22]"
        } else "[]"

        SettingsEnum.SB_UUID.let {
            if (it.isNull) it.saveValue(generateUserId())
        }
    }

    private fun SharedPreferences.findBehaviour(key: String): SegmentBehaviour? {
        val prefKey = getString(key, null) ?: return null
        for (behaviour in SegmentBehaviour.values()) {
            if (behaviour.key == prefKey) {
                return behaviour
            }
        }
        return null
    }

    private fun generateUserId() =
        ("${UUID.randomUUID()}${UUID.randomUUID()}${UUID.randomUUID()}")
            .replace("-", "")

    enum class SegmentBehaviour(
        val key: String,
        val desktopKey: Int,
        val ref: StringRef,
        val skip: Boolean,
        val showOnTimeBar: Boolean
    ) {
        SKIP_AUTOMATICALLY_ONCE(
            "skip-once",
            3,
            sf("skip_automatically_once"),
            true,
            true
        ),
        SKIP_AUTOMATICALLY(
            "skip",
            2,
            sf("skip_automatically"),
            true,
            true
        ),
        MANUAL_SKIP(
            "manual-skip",
            1,
            sf("skip_showbutton"),
            false,
            true
        ),
        IGNORE("ignore", -1, sf("skip_ignore"), false, false);

        companion object {
            @JvmStatic
            fun byDesktopKey(desktopKey: Int): SegmentBehaviour? {
                for (behaviour in SegmentBehaviour.values()) {
                    if (behaviour.desktopKey == desktopKey) {
                        return behaviour
                    }
                }
                return null
            }
        }
    }

    enum class SegmentInfo(
        val key: String,
        val title: StringRef,
        val skipMessage: StringRef,
        val description: StringRef,
        var behaviour: SegmentBehaviour,
        val defaultColor: Int
    ) {
        SPONSOR(
            "sponsor",
            sf("segments_sponsor"),
            sf("skipped_sponsor"),
            sf("segments_sponsor_sum"),
            SegmentBehaviour.SKIP_AUTOMATICALLY,
            -0xff2c00
        ),
        INTRO(
            "intro",
            sf("segments_intermission"),
            sf("skipped_intermission"),
            sf("segments_intermission_sum"),
            SegmentBehaviour.MANUAL_SKIP,
            -0xff0001
        ),
        OUTRO(
            "outro",
            sf("segments_endcards"),
            sf("skipped_endcard"),
            sf("segments_endcards_sum"),
            SegmentBehaviour.MANUAL_SKIP,
            -0xfdfd13
        ),
        INTERACTION(
            "interaction",
            sf("segments_subscribe"),
            sf("skipped_subscribe"),
            sf("segments_subscribe_sum"),
            SegmentBehaviour.SKIP_AUTOMATICALLY,
            -0x33ff01
        ),
        SELF_PROMO(
            "selfpromo",
            sf("segments_selfpromo"),
            sf("skipped_selfpromo"),
            sf("segments_selfpromo_sum"),
            SegmentBehaviour.SKIP_AUTOMATICALLY,
            -0x100
        ),
        MUSIC_OFFTOPIC(
            "music_offtopic",
            sf("segments_nomusic"),
            sf("skipped_nomusic"),
            sf("segments_nomusic_sum"),
            SegmentBehaviour.MANUAL_SKIP,
            -0x6700
        ),
        PREVIEW(
            "preview",
            sf("segments_preview"),
            sf("skipped_preview"),
            sf("segments_preview_sum"),
            DEFAULT_BEHAVIOUR,
            -0xff702a
        ),
        FILLER(
            "filler",
            sf("segments_filler"),
            sf("skipped_filler"),
            sf("segments_filler_sum"),
            DEFAULT_BEHAVIOUR,
            -0x8cff01
        ),
        UNSUBMITTED(
            "unsubmitted",
            StringRef.empty,
            sf("skipped_unsubmitted"),
            StringRef.empty,
            SegmentBehaviour.SKIP_AUTOMATICALLY,
            -0x1
        );

        val paint = Paint()
        var color = defaultColor
            set(value) {
                field = value and 0xFFFFFF
                paint.color = field
                paint.alpha = 255
            }

        val titleWithDot: CharSequence
            get() = Html.fromHtml(
                String.format(
                    "<font color=\"#%06X\">⬤</font> %s",
                    color,
                    title
                )
            )

        fun unsubmitted() = this == UNSUBMITTED

        companion object {
            @JvmStatic
            val submittableSegments = arrayOf(
                SPONSOR,
                INTRO,
                OUTRO,
                INTERACTION,
                SELF_PROMO,
                MUSIC_OFFTOPIC,
                PREVIEW,
                FILLER
            )

            private val valuesMap = mutableMapOf<String, SegmentInfo>()

            init {
                for (sm in submittableSegments) {
                    valuesMap[sm.key] = sm
                }
            }

            @JvmStatic
            fun byCategoryKey(key: String): SegmentInfo? {
                return valuesMap[key]
            }
        }
    }
}