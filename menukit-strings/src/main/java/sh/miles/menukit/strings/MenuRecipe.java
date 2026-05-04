package sh.miles.menukit.strings;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import sh.miles.menukit.util.PagedArray;
import sh.miles.menukit.util.PagedInventory;

/**
 * Represents a "recipe'. The idea of a recipe is a string to {@link MenuStack} representation of a menu. Each recipe
 * uses characters to map between items and MenuStack.
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class MenuRecipe {
    private final Char2ObjectMap<@Nullable MenuStack> mapping;
    private final PagedArray<Character> pattern;

    private MenuRecipe(final Char2ObjectMap<@Nullable MenuStack> mapping, final PagedArray<Character> pattern) {
        this.mapping = mapping;
        this.pattern = pattern;
    }

    /**
     * Applies this MenuRecipe to a PagedInventory.
     *
     * @param inventory the inventory the recipe is being applied to
     * @since 1.0.0-SNAPSHOT
     */
    public void apply(PagedInventory inventory) {
        Preconditions.checkArgument(inventory != null, "The provided inventory must not be null");
        Preconditions.checkArgument(this.pattern.getPageSize() <= inventory.getPageSize() || this.pattern.getPages() <= inventory.getPages(), "The pattern can not be applied to this inventory because the pattern is too large");

        for (int page = 0; page < this.pattern.getPages(); page++) {
            for (int index = 0; index < this.pattern.getPageSize(); index++) {
                final var key = this.pattern.get(index, page);
                if (key == null) continue;
                final MenuStack stack = this.mapping.get((char) key);
                if (stack == null) {
                    throw new IllegalStateException("No found mapping for key " + key);
                }
                inventory.setItem(stack.transfer().page(page).index(index).inventory(inventory).build());
            }
        }
    }

    /**
     * Gets the menu stack for the given character key
     *
     * @param key the key
     * @return the menu stack or null if no menu stack maps to that key
     * @since 1.0.0-SNAPSHOT
     */
    @Nullable
    public MenuStack getMenuStack(char key) {
        return this.mapping.get(key);
    }

    /**
     * Takes in a page number and returns a single page mapping in form of a char array to you as a result.
     *
     * @param page the page to index the mapping for
     * @return a char array of the indexed page
     * @since 1.0.0-SNAPSHOT
     */
    public char[] getPagePattern(int page) {
        char[] singlePage = new char[this.pattern.getPageSize()];
        for (int i = 0; i < this.pattern.getPageSize(); i++) {
            singlePage[i] = this.pattern.get(i, page);
        }

        return singlePage;
    }

    /**
     * Creates a new builder to create a MenuRecipe with.
     *
     * @return the newly created builder
     * @since 1.0.0-SNAPSHOT
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A Builder class used to easily create new {@link MenuRecipe}'s.
     *
     * @since 1.0.0-SNAPSHOT
     */
    public static class Builder {

        private final Char2ObjectMap<MenuStack> mapping = new Char2ObjectOpenHashMap<>();
        private final Int2ObjectMap<String> pages = new Int2ObjectOpenHashMap<>();
        private int pageSize = -999;

        private Builder() {
        }

        /**
         * Creates a new "page". Each page is represented by a layout string of size "n" where n is a standardized size
         * of all pages.
         *
         * @param page   the page number to create
         * @param layout the layout
         * @return this builder
         */
        public Builder page(int page, String layout) {
            this.pages.put(page, layout);
            this.pageSize = layout.replaceAll("\n", "").length();
            return this;
        }

        /**
         * Maps a character key to a given MenuStack value.
         *
         * @param key   the character key
         * @param value the value to map that key to
         * @return this builder
         */
        public Builder map(char key, MenuStack value) {
            this.mapping.put(key, value);
            return this;
        }

        /**
         * Builds the {@link MenuRecipe} from the given information above.
         *
         * @return a valid {@link MenuRecipe} if the build was a success
         * @throws IllegalStateException thrown if no pages were created
         */
        public MenuRecipe build() throws IllegalStateException {
            final int maxPage = pages.keySet().intStream().max().orElse(-999);
            Preconditions.checkState(maxPage != -999, "Can not make MenuRecipe with 0 provided pages");
            final PagedArray<Character> pattern = new PagedArray<>(this.pageSize, maxPage + 1);
            this.pages.forEach((page, layout) -> {
                parseLayout(page, layout, pattern);
            });

            return new MenuRecipe(this.mapping, pattern);
        }

        /**
         * Parses a "page layout" into page, "page" in the given PagedArray. This is done by trimming the string and
         * replacing all "\n" with empty characters. This effectively trims the layout down to a single char array.
         *
         * @param page    the page to input the layout into
         * @param layout  the layout string
         * @param pattern the pattern page array.
         */
        private void parseLayout(int page, String layout, PagedArray<Character> pattern) {
            layout = layout.trim().replaceAll("\n", "");
            Preconditions.checkState(layout.length() == this.pageSize, "Page size is not consistent with layout string");
            for (int i = 0; i < layout.length(); i++) {
                pattern.set(page, i, layout.charAt(i));
            }
        }
    }
}
