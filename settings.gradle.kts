pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("vendor/ics-openvpn/gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "aperture-vpn"

include(":app")
include(":openvpn")
project(":openvpn").projectDir = file("vendor/ics-openvpn/main")
