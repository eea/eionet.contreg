package eionet.cr.util;

import eionet.cr.ApplicationTestContext;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class ObjectLabelPairTest extends TestCase {

    @Test
    public void testCompraision() {
        ObjectLabelPair pair2 =
                new ObjectLabelPair("http://www.w3.org/2002/07/owl#NotDifferent", "http://www.w3.org/2002/07/owl#NotDifferent");
        ObjectLabelPair pair1 =
                new ObjectLabelPair("http://www.w3.org/2002/07/owl#AllDifferent", "http://www.w3.org/2002/07/owl#AllDifferent");

        assertTrue(pair1.compareTo(pair2) < 0);

    }

    @Test
    public void testCompraision2() {
        ObjectLabelPair pair2 = new ObjectLabelPair("http://www.w3.org/2002/07/owl#NotDifferent", "AAAA");
        ObjectLabelPair pair1 = new ObjectLabelPair("http://www.w3.org/2002/07/owl#AllDifferent", "MMMMMM");

        assertTrue(pair1.compareTo(pair2) > 0);

    }

    @Test
    public void testCompraision3() {
        ObjectLabelPair pair2 = new ObjectLabelPair("ZZZZZZ", "ZZZ");
        ObjectLabelPair pair1 = new ObjectLabelPair("AAAA", "AAAAAA");

        assertTrue(pair1.compareTo(pair2) < 0);

    }

}
