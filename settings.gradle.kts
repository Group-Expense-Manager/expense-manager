rootProject.name = "expense-manager"

dependencyResolutionManagement {
    versionCatalogs {
        create("tools") {
            version("jvm", "21")
            version("kotlin", "2.0.10")

            plugin("ktlint-core", "org.jlleitschuh.gradle.ktlint").version("12.1.2")

            plugin("detekt", "io.gitlab.arturbosch.detekt").version("1.23.7")

            plugin("scmversion", "com.intershop.gradle.scmversion").version("7.0.0")

            plugin("dependency-management", "io.spring.dependency-management").version("1.1.7")
            plugin("spring-boot", "org.springframework.boot").version("3.4.0")

            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin-spring", "org.jetbrains.kotlin.plugin.spring").versionRef("kotlin")
            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")

            bundle("kotlin", listOf("kotlin-stdlib", "kotlin-reflect"))
        }

        create("libs") {
            version("spring-boot", "3.4.0")
            version("dependency-management", "1.1.7")

            library("guava", "com.google.guava:guava:33.4.0-jre")
            library("kotlinlogging", "io.github.oshai:kotlin-logging-jvm:7.0.0")
            library("lib-gem", "pl.edu.agh.gem:lib-gem:0.5.2")

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
            version("kotest", "5.9.1")
            version("testcontainers", "1.20.4")
            library("kotest-runner-junit5", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-assertions-json", "io.kotest", "kotest-assertions-json").versionRef("kotest")
            library("kotest-property", "io.kotest", "kotest-property").versionRef("kotest")
            library("kotest-framework-datatest", "io.kotest", "kotest-framework-datatest").versionRef("kotest")
            library("kotest-testcontainers", "io.kotest.extensions", "kotest-extensions-testcontainers").version(
                "2.0.2",
            )
            library("mockito", "org.mockito.kotlin:mockito-kotlin:5.4.0")
            library("archunit", "com.tngtech.archunit:archunit-junit5:1.3.0")

            library("kotest-spring", "io.kotest.extensions:kotest-extensions-spring:1.3.0")
            library("kotest-wiremock", "io.kotest.extensions:kotest-extensions-wiremock:3.1.0")
            library("junit", "org.junit.jupiter:junit-jupiter-engine:5.11.4")
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
            library("detekt-formatting", "io.gitlab.arturbosch.detekt", "detekt-formatting").version("1.23.7")
            library("detekt-rules-libraries", "io.gitlab.arturbosch.detekt", "detekt-rules-libraries").version("1.23.7")
            library("detekt-rules-ruleauthors", "io.gitlab.arturbosch.detekt", "detekt-rules-ruleauthors").version("1.23.7")
            library("detekt-compiler-wrapper", "com.braisgabin.detekt", "kotlin-compiler-wrapper").version("0.0.4")
            library("detekt-faire", "com.faire", "faire-detekt-rules").version("0.4.0")
            library("detekt-hbmartin", "com.github.hbmartin", "hbmartin-detekt-rules").version("0.1.7")
            library("kure-potlin", "pl.setblack", "kure-potlin").version("0.7.0")
        }
    }
}
