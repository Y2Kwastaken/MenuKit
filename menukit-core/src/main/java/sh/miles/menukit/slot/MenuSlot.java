package sh.miles.menukit.slot;

import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import sh.miles.menukit.menu.MenuEventCallback;
import sh.miles.menukit.util.PagedInventory;

import java.util.function.Consumer;

/**
 * Represents a singular slot within a greater menu
 *
 * @since 1.0.0-SNAPSHOT
 */
public interface MenuSlot {
	MenuSlot DUMMY = new MenuSlot() {
		@Override
		public ItemStack getContent() {
			return ItemStack.empty();
		}

		@Override
		public void setContent(final ItemStack item) {
			throw new UnsupportedOperationException("Dummy Slot");
		}

		@Override
		public boolean hasContent() {
			return false;
		}

		@Override
		public void click(final MenuEventCallback<InventoryClickEvent> callback) {}

		@Override
		public void drag(final MenuEventCallback<InventoryDragEvent> callback) {}

		@Override
		public int getSlot() {
			throw new UnsupportedOperationException("Dummy Slot");
		}

		@Override
		public int getPage() {
			throw new UnsupportedOperationException("Dummy Slot");
		}
	};

	/**
	 * Gets the content of this slot
	 *
	 * @return the content
	 * @since 1.0.0-SNAPSHOT
	 */
	ItemStack getContent();

	/**
	 * Sets the content of this slot
	 *
	 * @param item the item to set
	 */
	void setContent(ItemStack item);

	/**
	 * Gets whether or not this slot has an item
	 *
	 * @return true if this slot has an item
	 * @since 1.0.0-SNAPSHOT
	 */
	boolean hasContent();

	/**
	 * Called when the slot is clicked
	 *
	 * @param callback the event callback
	 * @since 1.0.0-SNAPSHOT
	 */
	void click(MenuEventCallback<InventoryClickEvent> callback);

	/**
	 * Called when the slot is dragged on
	 *
	 * @param callback the event callback
	 * @since 1.0.0-SNAPSHOT
	 */
	void drag(MenuEventCallback<InventoryDragEvent> callback);

	/**
	 * Gets the slot
	 *
	 * @return the slot index
	 * @since 1.0.0-SNAPSHOT
	 */
	int getSlot();

	/**
	 * Gets the page this slot is on
	 *
	 * @return the page index
	 * @since 1.0.0-SNAPSHOT
	 */
	int getPage();

	static MenuSlot.Builder builder() {
		return new Builder();
	}

	/**
	 * General builder for MenuSlots
	 *
	 * @since 1.0.0-SNAPSHOT
	 */
	class Builder {
		private int index = -999;
		private int page = -999;
		private PagedInventory inventory = null;
		private ItemStack content = ItemStack.empty();
		private Consumer<MenuEventCallback<InventoryClickEvent>> click = MenuEventCallback.CLICK_NOTHING;
		private Consumer<MenuEventCallback<InventoryDragEvent>> drag = MenuEventCallback.DRAG_NOTHING;

		private Builder() {}

		/**
		 * Sets the index of this slot builder. This field is required
		 *
		 * @param index the index, must be within the bounds of the provided {@link
		 *     #inventory(PagedInventory)}
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder index(final int index) {
			this.index = index;
			return this;
		}

		/**
		 * Sets the page this slot is on. This field is required
		 *
		 * @param page the page, must be within the page count of the provided {@link
		 *     #inventory(PagedInventory)}
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder page(final int page) {
			this.page = page;
			return this;
		}

		/**
		 * Sets the content of this slot
		 *
		 * @param itemStack the item content
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder content(final ItemStack itemStack) {
			Preconditions.checkArgument(itemStack != null, "The provided item must not be null");
			this.content = itemStack;
			return this;
		}

		/**
		 * Sets the inventory of this builder. This field is required
		 *
		 * @param inventory the inventory
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder inventory(final PagedInventory inventory) {
			Preconditions.checkArgument(inventory != null, "The provided inventory must not be null");
			this.inventory = inventory;
			return this;
		}

		/**
		 * Sets the click callback for this builder.
		 *
		 * @param click the click callback
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder click(final Consumer<MenuEventCallback<InventoryClickEvent>> click) {
			Preconditions.checkArgument(click != null, "The provided click event must not be null");
			this.click = click;
			return this;
		}

		/**
		 * Sets the drag callback for this builder.
		 *
		 * @param drag the drag callback
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder drag(final Consumer<MenuEventCallback<InventoryDragEvent>> drag) {
			Preconditions.checkArgument(drag != null, "The provided drag event must not be null");
			this.drag = drag;
			return this;
		}

		/**
		 * Sets both the {@link #click(MenuEventCallback)} and {@link #drag(MenuEventCallback)}
		 * properties of this builder to just cancel the provided event.
		 *
		 * <p>Using this wipes any builders set in {@link #click(MenuEventCallback)} and {@link
		 * #drag(MenuEventCallback)} no exceptions.
		 *
		 * @return this builder
		 * @since 1.0.0-SNAPSHOT
		 */
		public Builder disableInteractions() {
			this.click = MenuEventCallback.CLICK_CANCEL;
			this.drag = MenuEventCallback.DRAG_CANCEL;
			return this;
		}

		public MenuSlot build() {
			Preconditions.checkArgument(
					this.inventory != null, "Can not build a MenuSlot without an assigned inventory");
			final int size = this.inventory.getPageSize();
			Preconditions.checkArgument(
					this.index >= 0 && this.index < size,
					"The provided index for the MenuSlot must be within the bounds of the provided" + " inventory");
			final int pageCount = this.inventory.getPages();
			Preconditions.checkArgument(
					this.page >= 0 && this.page < pageCount,
					"The provided page for the MenuSlot must be within the bounds of the provided inventory"
							+ " page size");

			final var slotImpl = new MenuSlotImpl(this.index, this.page, this.inventory, this.click, this.drag);
			slotImpl.setContent(this.content);
			return slotImpl;
		}
	}
}
