pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "Kmemo"

include(":kmemo-core")
include(":kmemo-store-tck")
include(":kmemo-store-redis")
include(":kmemo-store-postgres")
include(":kmemo-store-hnsw")
include(":kmemo-micrometer")
include(":kmemo-slf4j")
include(":kmemo-benchmarks")
include(":kmemo-bom")
include(":kmemo-spring-boot-starter")
include(":kmemo-spring-ai")
include(":kmemo-langchain4j")
include(":kmemo-ktor")
include(":examples")
