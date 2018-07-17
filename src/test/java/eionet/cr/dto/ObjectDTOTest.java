package eionet.cr.dto;

import static org.junit.Assert.assertEquals;

import eionet.cr.ApplicationTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
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
