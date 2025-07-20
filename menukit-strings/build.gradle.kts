plugins {
    `java-library`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}

dependencies {
    compileOnly(libs.papermc)

    implementation(project(":menukit-core"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
