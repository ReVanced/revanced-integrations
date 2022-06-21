package app.revanced.integrations.adremover.whitelist;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import app.revanced.integrations.sponsorblock.SponsorBlockSettings;
import app.revanced.integrations.utils.SharedPrefNames;

public enum WhitelistType {
    ADS(SharedPrefNames.YOUTUBE, "vanced_whitelist_ads_enabled"),
    SPONSORBLOCK(SharedPrefNames.SPONSOR_BLOCK, "vanced_whitelist_sb_enabled");

    private final String friendlyName;
    private final String preferencesName;
    private final String preferenceEnabledName;
    private final SharedPrefNames name;

    WhitelistType(SharedPrefNames name, String preferenceEnabledName) {
        this.friendlyName = str("vanced_whitelisting_" + name().toLowerCase());
        this.name = name;
        this.preferencesName = "whitelist_" + name();
        this.preferenceEnabledName = preferenceEnabledName;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public SharedPrefNames getSharedPreferencesName() {
        return name;
    }

    public String getPreferencesName() {
        return preferencesName;
    }

    public String getPreferenceEnabledName() {
        return preferenceEnabledName;
    }
}