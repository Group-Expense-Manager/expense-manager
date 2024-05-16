rootProject.name = "service-template"

dependencyResolutionManagement {
    versionCatalogs {
        create("tools") {
            version("jvm", "21")
            version("kotlin", "1.9.23")

            version("ktlint", "11.3.1")
            plugin("ktlint-core", "org.jlleitschuh.gradle.ktlint").versionRef("ktlint")
            plugin("ktlint-idea", "org.jlleitschuh.gradle.ktlint-idea").versionRef("ktlint")

            plugin("detekt", "io.gitlab.arturbosch.detekt").version("1.23.6")

            plugin("kover", "org.jetbrains.kotlinx.kover").version("0.6.1")

            plugin("scmversion", "com.intershop.gradle.scmversion").version("7.0.0")

            plugin("dependency-management", "io.spring.dependency-management").version("1.1.4")
            plugin("spring-boot", "org.springframework.boot").version("3.2.3")

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-spring", "org.jetbrains.kotlin.plugin.spring").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")

            bundle("kotlin", listOf("kotlin-stdlib", "kotlin-reflect"))
        }

        create("libs") {
            version("spring-boot", "3.2.3")
            version("dependency-management", "1.1.4")

            library("guava", "com.google.guava:guava:33.1.0-jre")
            library("kotlinlogging", "io.github.microutils:kotlin-logging:3.0.5")
            library("lib-gem", "pl.edu.agh.gem:lib-gem:0.1.4")

            version("resilience4j", "2.2.0")
            library("resilience4j-kotlin", "io.github.resilience4j", "resilience4j-kotlin").versionRef("resilience4j")
            library("resilience4j-retry", "io.github.resilience4j", "resilience4j-retry").versionRef("resilience4j")
            library("resilience4j-spring", "io.github.resilience4j", "resilience4j-spring-boot3").versionRef("resilience4j")

            bundle(
                "resilience4j",
                listOf(
                    "resilience4j-kotlin",
                    "resilience4j-retry",
                    "resilience4j-spring",
                ),
            )
        }
        create("testlibs") {
            version("kotest", "5.8.1")
            version("testcontainers", "1.19.7")
            library("kotest-runner-junit5", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-assertions-json", "io.kotest", "kotest-assertions-json").versionRef("kotest")
            library("kotest-property", "io.kotest", "kotest-property").versionRef("kotest")
            library("kotest-framework-datatest", "io.kotest", "kotest-framework-datatest").versionRef("kotest")
            library("kotest-testcontainers", "io.kotest.extensions", "kotest-extensions-testcontainers").version(
                "2.0.2",
            )
            library("mockito", "org.mockito.kotlin:mockito-kotlin:5.2.1")
            library("archunit", "com.tngtech.archunit:archunit-junit5:1.2.1")

            library("kotest-spring", "io.kotest.extensions:kotest-extensions-spring:1.1.3")
            library("kotest-wiremock", "io.kotest.extensions:kotest-extensions-wiremock:3.0.1")
            library("junit", "org.junit.jupiter:junit-jupiter-engine:5.10.2")
            library("testcontainers-core", "org.testcontainers", "testcontainers").versionRef("testcontainers")
            library("testcontainers-mongodb", "org.testcontainers", "mongodb").versionRef("testcontainers")

            bundle(
                "kotest-core",
                listOf(
                    "kotest-runner-junit5",
                    "kotest-assertions-core",
                    "kotest-assertions-json",
                    "kotest-property",
                    "kotest-framework-datatest",
                ),
            )

            bundle(
                "kotest-extensions",
                listOf(
                    "kotest-spring",
                    "kotest-wiremock",
                    "kotest-testcontainers",

                ),
            )

            bundle(
                "testcontainers",
                listOf(
                    "testcontainers-core",
                    "testcontainers-mongodb",
                ),
            )
        }

        create("detectlibs") {
            library("detekt-formatting", "io.gitlab.arturbosch.detekt", "detekt-formatting").version("1.23.6")
            library("detekt-rules-libraries", "io.gitlab.arturbosch.detekt", "detekt-rules-libraries").version("1.23.6")
            library("detekt-rules-ruleauthors", "io.gitlab.arturbosch.detekt", "detekt-rules-ruleauthors").version("1.23.6")
            library("detekt-compiler-wrapper", "com.braisgabin.detekt", "kotlin-compiler-wrapper").version("0.0.4")
            library("detekt-faire", "com.github.Faire", "faire-detekt-rules").version("0.1.1")
            library("detekt-hbmartin", "com.github.hbmartin", "hbmartin-detekt-rules").version("0.1.4")
            library("kure-potlin", "pl.setblack", "kure-potlin").version("0.7.0")
        }
    }
}
