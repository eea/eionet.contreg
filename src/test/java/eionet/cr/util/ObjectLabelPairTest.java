package eionet.cr.util;

import junit.framework.TestCase;

public class ObjectLabelPairTest extends TestCase {

    public void testCompraision() {
        ObjectLabelPair pair2 =
                new ObjectLabelPair("http://www.w3.org/2002/07/owl#NotDifferent", "http://www.w3.org/2002/07/owl#NotDifferent");
        ObjectLabelPair pair1 =
                new ObjectLabelPair("http://www.w3.org/2002/07/owl#AllDifferent", "http://www.w3.org/2002/07/owl#AllDifferent");

        assertTrue(pair1.compareTo(pair2) < 0);

    }

    public void testCompraision2() {
        ObjectLabelPair pair2 = new ObjectLabelPair("http://www.w3.org/2002/07/owl#NotDifferent", "AAAA");
        ObjectLabelPair pair1 = new ObjectLabelPair("http://www.w3.org/2002/07/owl#AllDifferent", "MMMMMM");

        assertTrue(pair1.compareTo(pair2) > 0);

    }

    public void testCompraision3() {
        ObjectLabelPair pair2 = new ObjectLabelPair("ZZZZZZ", "ZZZ");
        ObjectLabelPair pair1 = new ObjectLabelPair("AAAA", "AAAAAA");

        assertTrue(pair1.compareTo(pair2) < 0);

    }

}
