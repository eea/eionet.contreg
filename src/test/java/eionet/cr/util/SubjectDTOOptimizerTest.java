package eionet.cr.util;

import java.util.List;

import eionet.cr.ApplicationTestContext;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 *
 * @author Jaanus Heinlaid
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class SubjectDTOOptimizerTest extends TestCase {

    /**
     *
     */
    @Test
    public void testAcceptedLanguages() {

        List<String> languages = Util.getAcceptedLanguages("et,pl;q=0.5,dk,ru;q=0.7");

        assertNotNull(languages);
        assertEquals(6, languages.size());
        assertEquals("et", languages.get(0));
        assertEquals("dk", languages.get(1));
        assertEquals("ru", languages.get(2));
        assertEquals("pl", languages.get(3));
        assertEquals("en", languages.get(4));
        assertEquals("", languages.get(5));
    }

}
