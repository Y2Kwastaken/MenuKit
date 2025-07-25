plugins {
    java
    alias(libs.plugins.run.paper)
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly(libs.papermc)

    implementation(project(":menukit-core"))
    implementation(project(":menukit-strings"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.runServer {
    minecraftVersion("1.21.8")
}
