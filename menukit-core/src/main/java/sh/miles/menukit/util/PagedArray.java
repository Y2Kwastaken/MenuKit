package sh.miles.menukit.util;

import org.jspecify.annotations.Nullable;

/**
 * Data structure consisting of a backing array exposed as a single array and "pages" the current page can be adjusted
 * for each index in the array individually.
 * <p>
 * Like normal arrays paged arrays can not change sizes and must be completely re-allocated to do so. Determine the
 * number of pages carefully with that in mind.
 *
 * @since 1.0.0-SNAPSHOT
 */
public final class PagedArray<T> {

    private final Object[][] array;
    /*
     * this array is used to determine which "page" each slot is on
     */
    private final int[] pageStats;
    private final int pageSize;
    private final int pages;

    /**
     * Creates a new paged array
     *
     * @param pageSize the size of each page
     * @param pages    the number of pages
     * @throws IllegalStateException thrown if either parameter is less than or equal to 0
     * @since 1.0.0-SNAPSHOT
     */
    public PagedArray(int pageSize, int pages) throws IllegalStateException {
        if (pageSize <= 0) throw new IllegalStateException("Can not allocate 0 or less sized page array");
        if (pages <= 0) throw new IllegalStateException("Can not allocate 0 or less pages");

        this.array = new Object[pages][pageSize];
        this.pageStats = new int[pageSize];
        this.pageSize = pageSize;
        this.pages = pages;
    }

    /**
     * Gets the content from the index provided on the selected page for that index.
     * <p>
     * The page can be changed using {@link #setCurrentPageFor(int, int)} or {@link #setCurrentPage(int)}
     *
     * @param index the index
     * @throws IllegalStateException if the input is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    @Nullable
    public T get(int index) throws IllegalStateException {
        assertBoundsPageStats(index);
        return (T) this.array[this.pageStats[index]][index];
    }

    /**
     * Gets the content from the index provided on the provided page
     *
     * @param index the index to get the object from
     * @param page  the page to get the object from
     * @return the previous object at that index if any
     * @throws IllegalStateException thrown if the input is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    @Nullable
    public T get(int index, int page) throws IllegalStateException {
        assertBoundsPageStats(index);
        assertBoundsPages(page);

        return (T) this.array[page][index];
    }

    /**
     * Sets the value at the index provided on the currently selected page for that index.
     * <p>
     * The page can be changed using {@link #setCurrentPageFor(int, int)} or {@link #setCurrentPage(int)}
     *
     * @param index the index
     * @param value the value to set at that index on its currently selected page
     * @return the last object associated with that index
     * @throws IllegalStateException thrown if the index is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    @Nullable
    public T set(int index, @Nullable T value) throws IllegalStateException {
        final T previous = get(index); // get does bounds check
        this.array[this.pageStats[index]][index] = value;
        return previous;
    }

    /**
     * Sets the value at a specific index on the provided page
     *
     * @param page  the page to set the object on
     * @param index the index on the given page to set
     * @param value the object to put
     * @return the last object associated with that page and index if any
     * @throws IllegalStateException thrown if the index is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    @Nullable
    public T set(int page, int index, @Nullable T value) throws IllegalStateException {
        final T previous = get(index, page);
        this.array[page][index] = value;
        return previous;
    }

    /**
     * Sets the current page for the provided index
     *
     * @param index the index to set
     * @param page  the page to flip to
     * @throws IllegalStateException if the input is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPageFor(int index, int page) throws IllegalStateException {
        assertBoundsPages(page);
        assertBoundsPageStats(index);
        pageStats[index] = page;
    }

    /**
     * Sets all indexes to the current page
     *
     * @param page the page to flip to
     * @throws IllegalStateException thrown if the page input is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    public void setCurrentPage(int page) throws IllegalStateException {
        assertBoundsPages(page);
        for (int index = 0; index < pageSize; index++) {
            pageStats[index] = page;
        }
    }

    /**
     * Gets the page that the given index is on
     *
     * @param index the index
     * @return the page that index is on, 0 for first page.
     * @throws IllegalStateException if the input is out of bounds
     * @since 1.0.0-SNAPSHOT
     */
    public int getCurrentPage(int index) throws IllegalStateException {
        assertBoundsPageStats(index);
        return pageStats[index];
    }

    /**
     * Gets the number of pages
     *
     * @return the number of pages
     * @since 1.0.0-SNAPSHOT
     */
    public int getPages() {
        return this.pages;
    }

    /**
     * Gets the page size
     *
     * @return the page size
     * @since 1.0.0-SNAPSHOT
     */
    public int getPageSize() {
        return this.pageSize;
    }

    private void assertBoundsPageStats(int index) {
        if (index < 0 || index > pageStats.length) {
            throw new IllegalStateException("Out of bounds for getting current page for an index given value %d is not within 0 and %d".formatted(index, pageStats.length));
        }
    }

    private void assertBoundsPages(int page) {
        if (page < 0 || page >= this.pages) {
            throw new IllegalStateException("Out of bounds for amount of pages available index given value %d is not within 0 and %d".formatted(page, this.pages));
        }
    }
}
