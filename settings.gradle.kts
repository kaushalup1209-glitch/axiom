// ===== SECTION: SETTINGS_PLUGIN_REPOSITORIES =====
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
// ===== END SECTION: SETTINGS_PLUGIN_REPOSITORIES =====

// ===== SECTION: SETTINGS_DEPENDENCY_REPOSITORIES =====
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
// ===== END SECTION: SETTINGS_DEPENDENCY_REPOSITORIES =====

// ===== SECTION: SETTINGS_MODULES =====
rootProject.name = "Axiom"
include(":app")
// ===== END SECTION: SETTINGS_MODULES =====
