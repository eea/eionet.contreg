package eionet.cr.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

public class ObjectDTOTest {

    @Test
    public void testKnownSchemaLabel() {
        URI datatype = new URIImpl("http://www.w3.org/2001/XMLSchema#integer");

        ObjectDTO obj = new ObjectDTO("123", true, datatype);
        String dataTypeLabel = obj.getDataTypeLabel();

        assertEquals("xsd:integer", dataTypeLabel);
    }

    @Test
    public void testUnKnownSchemaLabel() {
        URI datatype = new URIImpl("http://Myschema#mydatatype");

        ObjectDTO obj = new ObjectDTO("blahh", true, datatype);
        String dataTypeLabel = obj.getDataTypeLabel();

        assertEquals("http://Myschema#mydatatype", dataTypeLabel);
    }

}
