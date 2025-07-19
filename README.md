# MenuKit

Using MenuKit core is very easy just shade and relocate

```gradle
maven {
    name = "milesReposSnapshots"
    url = uri("https://maven.miles.sh/snapshots")
}


implementation("sh.miles.menukit:menukit-core:unspecified")
```

## Example Usage

```java
import io.papermc.paper.registry.keys.SoundEventKeys;
import io.redlight.aio.paper.menu.MenuEventCallback;
import io.redlight.aio.paper.menu.SlotMenu;
import io.redlight.aio.paper.menu.SlotMenuFactory;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class TestMenu extends SlotMenu<InventoryView> {

	@Nullable
	private static SlotMenuFactory<InventoryView> factory = null;

	protected TestMenu(final Player player, final Function<Player, InventoryView> viewFactory, final int pageCount) {
		super(player, viewFactory, pageCount);
	}

	@Override
	protected void reload(final InventoryView view) {
		inventory.setItem(super.createSlot((builder) -> builder
			.index(0)
			.page(0)
			.content(ItemType.ARROW.createItemStack())
			.drag(MenuEventCallback.DRAG_CANCEL)
			.click(callback -> {
				callback.getEvent().setCancelled(true);
				final var player = callback.getPlayer();
				if (inventory.getCurrentPage(9) >= inventory.getPages() - 1) {
					player.playSound(Sound.sound(SoundEventKeys.BLOCK_REDSTONE_TORCH_BURNOUT.key(), Sound.Source.UI, 1.0f, 1.0f));
					return;
				}

				inventory.setCurrentPageFor(inventory.getCurrentPage(9) + 1, 9);
				player.playSound(Sound.sound(SoundEventKeys.ITEM_BOOK_PAGE_TURN.key(), Sound.Source.UI, 1.0f, 1.0f));

			})
			.build()));
		initContents();
		inventory.setItem(super.createSlot(builder -> builder
			.index(18)
			.page(0)
			.content(ItemType.ARROW.createItemStack())
			.drag(MenuEventCallback.DRAG_CANCEL)
			.click(callback -> {
				callback.getEvent().setCancelled(true);
				final var player = callback.getPlayer();
				if (inventory.getCurrentPage(9) <= 0) {
					player.playSound(Sound.sound(SoundEventKeys.BLOCK_REDSTONE_TORCH_BURNOUT.key(), Sound.Source.UI, 1.0f, 1.0f));
					return;
				}

				inventory.setCurrentPageFor(inventory.getCurrentPage(9) - 1, 9);
				player.playSound(Sound.sound(SoundEventKeys.ITEM_BOOK_PAGE_TURN.key(), Sound.Source.UI, 1.0f, 1.0f));
			})
		));
	}

	private void initContents() {
		inventory.setItem(super.createSlot(builder -> builder
			.index(9)
			.page(0)
			.content(ItemType.STONE.createItemStack(1))
			.drag(MenuEventCallback.DRAG_CANCEL)
			.click(MenuEventCallback.CLICK_CANCEL)
		));
		inventory.setItem(super.createSlot(builder -> builder
			.index(9)
			.page(1)
			.content(ItemType.STONE.createItemStack(2))
			.drag(MenuEventCallback.DRAG_CANCEL)
			.click(MenuEventCallback.CLICK_CANCEL)
		));
		inventory.setItem(super.createSlot(builder -> builder
			.index(9)
			.page(2)
			.content(ItemType.STONE.createItemStack(3))
			.drag(MenuEventCallback.DRAG_CANCEL)
			.click(MenuEventCallback.CLICK_CANCEL)
		));
	}

	public static SlotMenuFactory<InventoryView> factory() {
		if (factory == null) {
			factory = new SlotMenuFactory<>(MenuType.GENERIC_9X3::create, 3);
			factory.setMenuConstructor(TestMenu::new);
		}
		return factory;
	}
}

```
