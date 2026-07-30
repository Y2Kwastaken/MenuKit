plugins {
    `java-library`
    `maven-publish`
}

version = rootProject.version

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

/*
 * Mockito has to be attached as a java agent at test JVM startup. Left to self attach it warns, and the JDK will
 * refuse to load agents dynamically outright in a future release.
 */
val mockitoAgent: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

/*
 * The tests mock the Bukkit API, which main only depends on at compile time. compileOnly does not reach the test
 * source set on its own, so the test classpath inherits it rather than declaring paper a second time.
 */
configurations.testImplementation {
    extendsFrom(configurations.compileOnly.get())
}

dependencies {
    compileOnly(libs.papermc)
    api(libs.jspecify)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // resolved on its own so that the agent argument below points at exactly one jar
    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}", "-Xshare:off")
}

publishing {
    repositories {
        maven("https://maven.miles.sh/snapshots") {
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String
            from(components["java"])
        }
    }
}
