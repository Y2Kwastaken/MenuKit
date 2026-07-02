# MenuKit

MenuKit is a modular, efficient, and declarative inventory GUI library for PaperMC/Spigot. It abstracts the boilerplate of inventory management, offering a robust event handling system and a string-based layout engine for intuitive menu design.

See more extensive documentation on my docs page https://miles.sh/docs/menu_kit/

## Installation

MenuKit is available via the Miles Repository.

### Maven
```xml
<repository>
    <id>miles-repos-snapshots</id>
    <name>Miles Repositories</name>
    <url>https://maven.miles.sh/snapshots</url>
</repository>

<dependency>
    <groupId>sh.miles.menukit</groupId>
    <artifactId>menukit-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>sh.miles.menukit</groupId>
    <artifactId>menukit-strings</artifactId>
    <version>1.3.0-SNAPSHOT</version>
</dependency>
```

### Gradle
```kotlin
repositories {
    maven {
        name "milesReposSnapshots"
        url "https://maven.miles.sh/snapshots"
    }
}

dependencies {
    implementation "sh.miles.menukit:menukit-core:1.0.0-SNAPSHOT"
    implementation "sh.miles.menukit:menukit-strings:1.3.0-SNAPSHOT"
}
```

---

## Modules

* **`menukit-core`**: The backbone of the library. Handles `SlotMenu` abstraction, `PagedInventory` management, and functional `MenuEventCallback` systems.
* **`menukit-strings`**: A layout engine allowing menus to be defined via visual text patterns (Strings) rather than raw slot indices.
