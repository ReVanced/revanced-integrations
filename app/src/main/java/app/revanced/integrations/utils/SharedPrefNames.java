package app.revanced.integrations.utils;

public enum SharedPrefNames {

    YOUTUBE("youtube"),
    RYD("ryd"),
    SPONSOR_BLOCK("sponsor-block");

    private final String name;

    SharedPrefNames(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
