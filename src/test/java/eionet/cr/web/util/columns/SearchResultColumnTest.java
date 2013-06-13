package eionet.cr.web.util.columns;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import junit.framework.TestCase;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

/**
 * A unit test for the {@link SearchResultColumn}
 *
 * @author jaanus
 */
public class SearchResultColumnTest extends TestCase {

    /**
     * @throws UnsupportedEncodingException
     * 
     */
    @Test
    public void test() throws UnsupportedEncodingException {

        String uri = "http://test.uri/";
        String label = "Test URI";
        String blankUri1 = "b10040";
        String blankUri2 = "_:b10040";
        String blankUri3 = "nodeID://b10040";

        SearchResultColumnExtender testClass = new SearchResultColumnExtender();
        String aHref = testClass.buildFactsheetLink(uri, false, label, true);
        assertTrue(aHref != null);
        assertTrue(aHref.contains("uri=" + URLEncoder.encode(uri, "UTF-8")));
        assertTrue(aHref.contains(">" + StringEscapeUtils.escapeXml(label) + "</a>"));
        assertTrue(aHref.contains("title=\"" + StringEscapeUtils.escapeXml(uri) + "\""));

        aHref = testClass.buildFactsheetLink(blankUri1, true, label, true);
        assertTrue(aHref != null && aHref.contains("uri=" + URLEncoder.encode(blankUri3, "UTF-8")));

        aHref = testClass.buildFactsheetLink(blankUri2, true, label, true);
        assertTrue(aHref != null && aHref.contains("uri=" + URLEncoder.encode(blankUri3, "UTF-8")));

        aHref = testClass.buildFactsheetLink(blankUri3, true, label, true);
        assertTrue(aHref != null && aHref.contains("uri=" + URLEncoder.encode(blankUri3, "UTF-8")));
    }

    /**
     * Just a dummy class for extending {@link SearchResultColumn} for the unit-testing purpose only.
     *
     * @author jaanus
     */
    class SearchResultColumnExtender extends SearchResultColumn {

        /*
         * (non-Javadoc)
         * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
         */
        @Override
        public String format(Object object) {
            return null;
        }

        /*
         * (non-Javadoc)
         * @see eionet.cr.web.util.columns.SearchResultColumn#getSortParamValue()
         */
        @Override
        public String getSortParamValue() {
            return null;
        }

    }
}
