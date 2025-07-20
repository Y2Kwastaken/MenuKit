package sh.miles.menukit.strings;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.Nullable;
import sh.miles.menukit.util.PagedArray;
import sh.miles.menukit.util.PagedInventory;

public final class MenuRecipe {
    private final Char2ObjectMap<@Nullable MenuStack> mapping;
    private final PagedArray<Character> pattern;

    private MenuRecipe(final Char2ObjectMap<@Nullable MenuStack> mapping, final PagedArray<Character> pattern) {
        this.mapping = mapping;
        this.pattern = pattern;
    }

    /**
     * Applies this MenuRecipe to a PagedInventory
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
     */
    @Nullable
    public MenuStack getMenuStack(char key) {
        return this.mapping.get(key);
    }

    public char[] getPagePattern(int page) {
        char[] singlePage = new char[this.pattern.getPageSize()];
        for (int i = 0; i < this.pattern.getPageSize(); i++) {
            singlePage[i] = this.pattern.get(i, page);
        }

        return singlePage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Char2ObjectMap<MenuStack> mapping = new Char2ObjectOpenHashMap<>();
        private final Int2ObjectMap<String> pages = new Int2ObjectOpenHashMap<>();
        private int pageSize = -999;

        private Builder() {
        }

        public Builder page(int page, String layout) {
            Preconditions.checkState(pageSize == -999 || layout.length() == (pageSize + 2), "The given layout must match the standardized page size");
            this.pages.put(page, layout);
            this.pageSize = layout.length() - 2;
            return this;
        }

        public Builder map(char key, MenuStack value) {
            this.mapping.put(key, value);
            return this;
        }

        public MenuRecipe build() throws IllegalStateException {
            final int maxPage = pages.keySet().intStream().max().orElse(-999);
            Preconditions.checkState(maxPage != -999, "Can not make MenuRecipe with 0 provided pages");
            final PagedArray<Character> pattern = new PagedArray<>(this.pageSize, maxPage + 1);
            this.pages.forEach((page, layout) -> {
                parseLayout(page, layout, pattern);
            });

            return new MenuRecipe(this.mapping, pattern);
        }

        private void parseLayout(int page, String layout, PagedArray<Character> pattern) {
            layout = layout.trim().replaceAll("\n", "");
            Preconditions.checkState(layout.length() == this.pageSize, "Page size is not consistent with layout string");
            for (int i = 0; i < layout.length(); i++) {
                pattern.set(page, i, layout.charAt(i));
            }
        }
    }
}
