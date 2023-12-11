rootProject.name = "revanced-integrations"

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}

include(":app")
include(":dummy")
