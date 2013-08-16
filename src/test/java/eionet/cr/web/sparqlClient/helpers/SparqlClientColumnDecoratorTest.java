package eionet.cr.web.sparqlClient.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.dao.virtuoso.VirtuosoBaseDAO;

/**
 * A unit test for {@link SparqlClientColumnDecoratorTest}.
 *
 * @author jaanus
 */
public class SparqlClientColumnDecoratorTest extends TestCase {

    /**
     *
     * @throws UnsupportedEncodingException
     */
    @Test
    public void test() throws UnsupportedEncodingException {

        SparqlClientColumnDecorator decorator = new SparqlClientColumnDecorator();

        String uri = "http://test.uri/";
        String blankUri = "b10040";

        HashMap<String, ResultValue> rowMap = new HashMap<String, ResultValue>();
        rowMap.put("prop1", new ResultValue("literal1", true));
        rowMap.put("prop2", new ResultValue(uri, true));
        rowMap.put("prop3", new ResultValue(uri, false));
        rowMap.put("prop4", new ResultValue(blankUri, false, true));

        String decoratedValue = decorator.getDecoratedValue(rowMap, "prop1", null);
        assertEquals("literal1", decoratedValue);

        decoratedValue = decorator.getDecoratedValue(rowMap, "prop2", null);
        assertEquals(uri, decoratedValue);

        decoratedValue = decorator.getDecoratedValue(rowMap, "prop3", null);
        assertTrue(decoratedValue != null && decoratedValue.startsWith("<a href=\""));
        assertTrue(decoratedValue.contains("uri=" + URLEncoder.encode(uri, "UTF-8")));

        decoratedValue = decorator.getDecoratedValue(rowMap, "prop4", null);
        assertTrue(decoratedValue != null && decoratedValue.startsWith("<a href=\""));
        assertTrue(decoratedValue.contains("uri=" + URLEncoder.encode(VirtuosoBaseDAO.VIRTUOSO_BNODE_PREFIX + blankUri, "UTF-8")));
    }
}
