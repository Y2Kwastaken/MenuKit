package sh.miles.menukit.strings;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        System.out.println(Set.of(1, 2, 0, 4, 5, 6, 7, 8, 9).stream().max(Comparator.comparingInt(Integer::intValue)));
        System.out.println("""
                123456789
                123456789
                123456789
                """.trim().length());
    }
}
