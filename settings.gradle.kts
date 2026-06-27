pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/linxu-link/android-convention-plugin")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }

        // 添加阿里云maven中央仓库镜像
        maven(url = "https://maven.aliyun.com/repository/central")
        // 添加阿里云google仓库镜像
        maven(url = "https://maven.aliyun.com/repository/google")
        // 添加阿里云gradle插件仓库镜像
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")

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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 添加阿里云maven中央仓库镜像
        maven(url = "https://maven.aliyun.com/repository/central")
        // 添加阿里云google仓库镜像
        maven(url = "https://maven.aliyun.com/repository/google")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")

rootProject.name = "Velaris"

include(":foundation:toolkit")
include(":foundation:ui")
include(":foundation:navigation")
include(":foundation:domain")
include(":foundation:model")
include(":foundation:database")
include(":foundation:designsystem")
include(":foundation:data")
include(":foundation:player")
include(":foundation:alarm")
include(":foundation:particle")
include(":foundation:ads")
include(":foundation:jetpack")
include(":foundation:testing")
include(":sync:work")

include(":feature:scene:api")
include(":feature:scene:impl")
include(":feature:sceneList:api")
include(":feature:sceneList:impl")
include(":feature:sceneEdit:api")
include(":feature:sceneEdit:impl")
include(":feature:settings:api")
include(":feature:settings:impl")
include(":feature:sceneControl:api")
include(":feature:sceneControl:impl")
