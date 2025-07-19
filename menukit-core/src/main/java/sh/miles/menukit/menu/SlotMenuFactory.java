package sh.miles.menukit.menu;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import sh.miles.menukit.util.PagedInventory;

import java.util.function.Function;

public class SlotMenuFactory<V extends InventoryView> {

    private final Function<Player, V> viewFactory;
    private final int pageCount;

    private MenuConstructor<V> constructor;

    public SlotMenuFactory(final Function<Player, V> viewFactory, final int pageCount) {
        Preconditions.checkArgument(viewFactory != null, "The provided viewFactory should not be null");
        this.viewFactory = viewFactory;
        this.pageCount = pageCount;
        this.constructor = null;
    }

    /**
     * Creates a new menu of type "FactorizedSlotMenu".
     * <p>
     * A factorized or simplified slot menu that allows for basic initializable logic, but nothing else. In order to use
     * a factory to build your own type of menu a menu constructor should be set in
     * {@link #setMenuConstructor(MenuConstructor)} then use the method {@link #create(Player)}.
     *
     * @param player        the player to assign this menu to
     * @param initializable the initialization logic of this simple menu
     * @return the slot menu
     * @since 1.0.0-SNAPSHOT
     */
    public SlotMenu<V> create(final Player player, final MenuInitializable<V> initializable) {
        Preconditions.checkArgument(player != null, "The provided player must not be null");
        Preconditions.checkArgument(initializable != null, "The provided initializable must not be null");
        return new FactorizedSlotMenu<>(player, this.viewFactory, this.pageCount, initializable);
    }

    /**
     * Creates a new menu designated by {@link #setMenuConstructor(MenuConstructor)}
     *
     * @param player the player to assign this menu to
     * @return the slot menu
     * @since 1.0.0-SNAPSHOT
     */
    public SlotMenu<V> create(final Player player) {
        Preconditions.checkArgument(this.constructor != null, "The constructor should not be null when using this method set it with" + " #setMenuConstructor");
        Preconditions.checkArgument(player != null, "The provided player must not be null");
        return this.constructor.construct(player, this.viewFactory, this.pageCount);
    }

    /**
     * Sets this factories menu constructor
     *
     * <p>Note you can only set the menu constructor of a given factory once. Trying to set it again
     * will throw an IllegalArgumentException
     *
     * @param constructor the menu constructor to be used in this factory
     * @throws IllegalArgumentException thrown if the constructor is already set
     */
    public void setMenuConstructor(final MenuConstructor<V> constructor) throws IllegalArgumentException {
        Preconditions.checkArgument(constructor != null, "the provided constructor must not be null");
        Preconditions.checkArgument(this.constructor == null, "The constructor of this factory can only be set once");
        this.constructor = constructor;
    }

    static class FactorizedSlotMenu<V extends InventoryView> extends SlotMenu<V> {

        private final MenuInitializable<V> initializable;

        protected FactorizedSlotMenu(final Player player, final Function<Player, V> viewFactory, final int pageCount, final MenuInitializable<V> initializable) {
            super(player, viewFactory, pageCount);
            this.initializable = initializable;
        }

        @Override
        protected void reload(final V view) {
            this.initializable.init(view, this.inventory);
        }
    }

    /**
     * Functional interface used to edit the preliminary factorized slot menu
     *
     * @param <V> the type of view
     * @since 1.0.0-SNAPSHOT
     */
    @FunctionalInterface
    public interface MenuInitializable<V extends InventoryView> {
        void init(V bukkitView, PagedInventory inventory);
    }

    /**
     * Functional interface used to create any type of menu to be set with the
     * {@link #setMenuConstructor(MenuConstructor)}
     *
     * @param <V> the type of view
     * @since 1.0.0-SNAPSHOT
     */
    @FunctionalInterface
    public interface MenuConstructor<V extends InventoryView> {
        SlotMenu<V> construct(final Player player, final Function<Player, V> viewFactory, final int pageCount);
    }
}
