# MenuKit

MenuKit is a modular, efficient, and declarative inventory GUI library for PaperMC/Spigot. It abstracts the boilerplate of inventory management, offering a robust event handling system and a string-based layout engine for intuitive menu design.

## Installation

MenuKit is available via the Miles Repository.

### Maven
```xml
<repository>
    <id>miles-repos-snapshots</id>
    <name>Miles Repositories</name>
    <url>[https://maven.miles.sh/snapshots](https://maven.miles.sh/snapshots)</url>
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
```groovy
repositories {
    maven {
        name "milesReposSnapshots"
        url "[https://maven.miles.sh/snapshots](https://maven.miles.sh/snapshots)"
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

---

## Usage

### 1. The Declarative Approach (MenuRecipes)
Instead of calculating slot integers, MenuKit allows you to "draw" your inventory using characters. This is done via `menukit-strings`.

```java
public class MyCustomMenu extends SlotMenu<InventoryView> {

    // 1. Define the Visual Layout
    private static final MenuRecipe RECIPE = MenuRecipe.builder()
            .page(0, """
                    BBBBBBBBB
                    B  X I  B
                    BBBBBBBBB""")
            // 2. Map Characters to Items (MenuStacks)
            .map('B', MenuStack.of(ItemType.BLACK_STAINED_GLASS_PANE, true, true)) // Border
            .map('I', MenuStack.builder()
                    .content(ItemType.DIAMOND)
                    .click(e -> {
                        e.cancel();
                        e.getPlayer().sendMessage("You clicked the Diamond!");
                    })
                    .drag(MenuEventCallback.DRAG_CANCEL)
                    .build())
            .map('X', MenuStack.of(ItemType.BARRIER, true)) // Non-clickable barrier
            .build();

    public MyCustomMenu(Player player) {
        // Initialize parent with a standard Chest View (9x3)
        super(player, (p) -> MenuType.GENERIC_9X3.create(p, Component.text("My Menu")), 1);
    }

    @Override
    protected void reload(InventoryView view) {
        // 3. Apply the recipe to the inventory
        RECIPE.apply(this.getInventory());
    }
}
```

### 2. The Manual Approach (Extending SlotMenu)
If you prefer not to use String recipes or need to calculate slot positions programmatically (e.g., mathematical patterns), you can extend `SlotMenu` and use the `createSlot` helper.

```java
public class CounterMenu extends SlotMenu<InventoryView> {

    private int counter = 0;

    public CounterMenu(Player player) {
        super(player, (p) -> MenuType.GENERIC_9X1.create(p, Component.text("Counter Menu")), 1);
    }

    @Override
    protected void reload(InventoryView view) {
        // Clear the inventory or set a background if needed
        // this.inventory is your PagedInventory instance

        // Example: specific slot placement using the createSlot helper
        // This helper automatically links the slot to the current inventory and page 0
        MenuSlot counterButton = createSlot((builder) -> builder
                .index(4) // Center slot
                .content(ItemType.REDSTONE_BLOCK, (stack) -> {
                    stack.setData(DataComponentTypes.ITEM_NAME, Component.text("Clicks: " + counter));
                })
                .click((e) -> {
                    e.cancel();
                    this.counter++;
                    // Recursively call open() to refresh the view with new data
                    final var cur = inventory.getSlot(4).getContent();
                    cur.setData(DataComponentTypes.ITEM_NAME, Component.text("Clicks: " + counter));
                    inventory.getSlot(4).setContent(cur);
                })
                .drag(MenuEventCallback.DRAG_CANCEL)
        );

        this.inventory.setItem(counterButton);
    }
}
```

### 3. Functional Event Handling
MenuKit moves away from massive `InventoryClickEvent` listeners in favor of functional callbacks attached directly to the Item/Slot.

You can use the `MenuStack` builder to attach logic:

```java
MenuStack.builder()
    .content(ItemType.EMERALD_BLOCK)
    .click((callback) -> {
        // Automatic casting and helper methods available
        callback.cancel(); 
        
        Player p = callback.getPlayer();
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        
        // Access the specific menu instance if needed
        if (callback.getMenu() instanceof MyCustomMenu myMenu) {
            // specific logic
        }
    })
    .drag(MenuEventCallback.DRAG_CANCEL) // Pre-defined constants for common actions
    .build();
```

### 4. Simple Factory Menus
For simple, static menus where creating a new class file is overkill, use the `SlotMenuFactory`:

```java
SlotMenuFactory<InventoryView> factory = new SlotMenuFactory<>(
    (p) -> MenuType.GENERIC_9X1.create(p, Component.text("Quick Menu")), 
    1
);

factory.create(player, (view, inventory) -> {
    // Direct access to the PagedInventory to set slots
    inventory.setItem(MenuSlot.builder()
        .inventory(inventory)
        .page(0)
        .index(4)
        .content(ItemType.APPLE.createItemStack())
        .disableInteractions()
        .build());
}).open();
```

---

## Architecture Overview

### `SlotMenu` & `PagedInventory`
Every menu is backed by a `PagedInventory`. This wrapper allows you to manage items across multiple virtual pages, even if the frontend GUI only shows one page at a time.

### `MenuSlot`
A `MenuSlot` represents a single position in the GUI. It holds:
* The `ItemStack` content.
* A `Consumer` for Click events.
* A `Consumer` for Drag events.

### `MenuRecipe`
Found in the `strings` module, this class parses a string grid (e.g., 9x3 characters) and maps them to `MenuStack` definitions. This ensures your code structure visually matches the resulting GUI structure.
