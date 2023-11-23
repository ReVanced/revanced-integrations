rootProject.name = "revanced-integrations"

buildCache {
    local {
        isEnabled = !System.getenv().containsKey("CI")
    }
}

include(":app")
include(":dummy")
