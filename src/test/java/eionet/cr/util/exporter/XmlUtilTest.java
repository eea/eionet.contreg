package eionet.cr.util.exporter;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.export.XmlUtil;

public class XmlUtilTest extends TestCase {

    @Test
    public void testEscapeElementName() {
        assertEquals(XmlUtil.getEscapedElementName("1invalidElem"), "_1invalidElem");
        assertEquals(XmlUtil.getEscapedElementName("xmlElem"), "_xmlElem");
        assertEquals(XmlUtil.getEscapedElementName(".Elem"), "_Elem");

        assertEquals(XmlUtil.getEscapedElementName("test:Elem1"), "test_Elem1");
        assertEquals(XmlUtil.getEscapedElementName("test.Elem"), "test_Elem");
        assertEquals(XmlUtil.getEscapedElementName("elem#"), "elem_");
        assertEquals(XmlUtil.getEscapedElementName("elem?"), "elem_");
        assertEquals(XmlUtil.getEscapedElementName("elem1  and  elem2"), "elem1__and__elem2");

        assertEquals(XmlUtil.getEscapedElementName("elem***"), "elem___");
        assertEquals(XmlUtil.getEscapedElementName(""), XmlUtil.INVALID_ELEMENT_NAME);
    }
}
