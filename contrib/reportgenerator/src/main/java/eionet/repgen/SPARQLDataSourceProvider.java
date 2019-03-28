package eionet.repgen;

import java.util.ArrayList;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignField;
import eionet.repgen.datasources.SPARQLDataSource;

public class SPARQLDataSourceProvider implements JRDataSourceProvider {

    @Override
    public JRDataSource create(JasperReport arg0) throws JRException {
        return new SPARQLDataSource();
        // return null;
    }

    @Override
    public void dispose(JRDataSource arg0) throws JRException {

    }

    @Override
    public JRField[] getFields(JasperReport arg0) throws JRException, UnsupportedOperationException {

        ArrayList fields = new ArrayList();

        JRDesignField field = new JRDesignField();
        field.setName("bookmark");
        field.setValueClassName("java.lang.String");
        fields.add(field);
        JRDesignField field2 = new JRDesignField();
        field2.setName("label");
        field2.setValueClassName("java.lang.String");
        fields.add(field2);

        return (JRField[]) fields.toArray(new JRField[fields.size()]);

    }

    @Override
    public boolean supportsGetFieldsOperation() {
        return false;
    }

}
