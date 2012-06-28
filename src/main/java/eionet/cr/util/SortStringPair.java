package eionet.cr.util;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public final class SortStringPair {

    /**
     * Hide utility class constructor.
     */
    private SortStringPair() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param sourcePairs
     * @return
     */
    public static List<Pair<String, String>> sortByLeftAsc(List<Pair<String, String>> sourcePairs) {
        // Sorting the types by Pair left.
        for (int i = 0; i < sourcePairs.size(); i++) {
            for (int j = i; j < sourcePairs.size(); j++) {
                if (sourcePairs.get(i).getLeft().compareToIgnoreCase(sourcePairs.get(j).getLeft()) > 0) {
                    Collections.swap(sourcePairs, i, j);
                }
            }
        }
        return sourcePairs;
    }

    /**
     *
     * @param sourcePairs
     * @return
     */
    public static List<Pair<String, String>> sortByRightAsc(List<Pair<String, String>> sourcePairs) {
        // Sorting the types by Pair right.
        for (int i = 0; i < sourcePairs.size(); i++) {
            for (int j = i; j < sourcePairs.size(); j++) {
                if (sourcePairs.get(i).getRight().compareToIgnoreCase(sourcePairs.get(j).getRight()) > 0) {
                    Collections.swap(sourcePairs, i, j);
                }
            }
        }
        return sourcePairs;
    }
}
