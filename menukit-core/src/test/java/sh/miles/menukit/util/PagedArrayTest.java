package sh.miles.menukit.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link PagedArray}, the structure that gives every index its own independently selected page.
 */
class PagedArrayTest {

    private static final int PAGE_SIZE = 9;
    private static final int PAGES = 3;

    private PagedArray<String> array() {
        return new PagedArray<>(PAGE_SIZE, PAGES);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void constructorRejectsNonPositivePageSize(final int pageSize) {
        assertThrows(IllegalStateException.class, () -> new PagedArray<String>(pageSize, PAGES));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void constructorRejectsNonPositivePageCount(final int pages) {
        assertThrows(IllegalStateException.class, () -> new PagedArray<String>(PAGE_SIZE, pages));
    }

    @Test
    void reportsTheDimensionsItWasBuiltWith() {
        final PagedArray<String> array = array();

        assertEquals(PAGE_SIZE, array.getPageSize());
        assertEquals(PAGES, array.getPages());
    }

    @Test
    void everyIndexStartsOnPageZeroAndEmpty() {
        final PagedArray<String> array = array();

        for (int index = 0; index < PAGE_SIZE; index++) {
            assertEquals(0, array.getCurrentPage(index));
            assertNull(array.get(index));
        }
    }

    @Test
    void valueSetOnAPageIsOnlyVisibleFromThatPage() {
        final PagedArray<String> array = array();

        array.set(1, 2, "value");

        assertEquals("value", array.get(1, 2));
        assertNull(array.get(0, 2));
        assertNull(array.get(2, 2));
    }

    @Test
    void indexedGetReadsFromThatIndexesCurrentPage() {
        final PagedArray<String> array = array();
        array.set(1, 2, "value");

        assertNull(array.get(2), "index 2 is still on page 0, so the page 1 value must not be visible");

        array.setCurrentPageFor(1, 2);

        assertEquals("value", array.get(2));
    }

    @Test
    void indexedSetWritesToThatIndexesCurrentPage() {
        final PagedArray<String> array = array();
        array.setCurrentPageFor(1, 2);

        array.set(2, "value");

        assertEquals("value", array.get(1, 2));
        assertNull(array.get(0, 2));
    }

    @Test
    void indexedSetReturnsThePreviousValue() {
        final PagedArray<String> array = array();

        assertNull(array.set(0, "first"));
        assertEquals("first", array.set(0, "second"));
        assertEquals("second", array.get(0));
    }

    @Test
    void pagedSetReturnsThePreviousValueOnThatPage() {
        final PagedArray<String> array = array();

        assertNull(array.set(1, 0, "first"));
        assertEquals("first", array.set(1, 0, "second"));
        assertEquals("second", array.get(1, 0));
    }

    @Test
    void setAcceptsNullToClearAValue() {
        final PagedArray<String> array = array();
        array.set(1, 0, "value");

        assertEquals("value", array.set(1, 0, null));
        assertNull(array.get(1, 0));
    }

    @Test
    void eachIndexTracksItsPageIndependently() {
        final PagedArray<String> array = array();
        array.set(0, 3, "three on zero");
        array.set(2, 3, "three on two");
        array.set(0, 4, "four on zero");

        array.setCurrentPageFor(2, 3);

        assertEquals(2, array.getCurrentPage(3));
        assertEquals(0, array.getCurrentPage(4), "moving index 3 must not drag any other index along");
        assertEquals("three on two", array.get(3));
        assertEquals("four on zero", array.get(4));
    }

    @Test
    void setCurrentPageMovesEveryIndex() {
        final PagedArray<String> array = array();
        array.setCurrentPageFor(2, 0);

        array.setCurrentPage(1);

        for (int index = 0; index < PAGE_SIZE; index++) {
            assertEquals(1, array.getCurrentPage(index));
        }
    }

    @Test
    void valuesSurviveBeingPagedAwayFromAndBackTo() {
        final PagedArray<String> array = array();
        array.set(0, 5, "home");
        array.set(1, 5, "away");

        array.setCurrentPageFor(1, 5);
        assertEquals("away", array.get(5));

        array.setCurrentPageFor(0, 5);
        assertEquals("home", array.get(5));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, PAGE_SIZE})
    void indexOperationsRejectOutOfBoundsIndexes(final int index) {
        final PagedArray<String> array = array();

        assertThrows(IllegalStateException.class, () -> array.get(index));
        assertThrows(IllegalStateException.class, () -> array.get(0, index));
        assertThrows(IllegalStateException.class, () -> array.set(index, "value"));
        assertThrows(IllegalStateException.class, () -> array.set(0, index, "value"));
        assertThrows(IllegalStateException.class, () -> array.setCurrentPageFor(0, index));
        assertThrows(IllegalStateException.class, () -> array.getCurrentPage(index));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, PAGES})
    void pageOperationsRejectOutOfBoundsPages(final int page) {
        final PagedArray<String> array = array();

        assertThrows(IllegalStateException.class, () -> array.get(page, 0));
        assertThrows(IllegalStateException.class, () -> array.set(page, 0, "value"));
        assertThrows(IllegalStateException.class, () -> array.setCurrentPageFor(page, 0));
        assertThrows(IllegalStateException.class, () -> array.setCurrentPage(page));
    }
}
