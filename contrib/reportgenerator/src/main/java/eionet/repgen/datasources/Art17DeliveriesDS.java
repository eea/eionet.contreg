package eionet.repgen.datasources;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

import eionet.repgen.SPARQLUtil;

/**
 * sample class for Art17 deliveries.
 *
 * @author Kaido Laine
 */
public class Art17DeliveriesDS extends SPARQLDataSource {

    private String countryCode;
    private String sparql;

    // calculated values:
    private int habitatsTerrestrial = 0;
    private int speciesTerrestrial = 0;
    private int habitatsMarine = 0;
    private int speciesMarine = 0;

    private int totalReports = 0;

    private boolean fieldsFilled = false;

    public Art17DeliveriesDS(String countryCode) throws Exception {

        // bookmark with name below has to exist in CR bookmarks:
        sparql = SPARQLUtil.getSparqlBookmarkByName("Art 17 reports delivered");
        sparql = SPARQLUtil.replaceSparqlParam(sparql, "countryCode", countryCode);

        this.countryCode = countryCode;
        init(sparql);
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {

        String fldName = field.getName();

        if (fldName.equals("habitatsTerrestrial")) {
            return habitatsTerrestrial;
        } else if (fldName.equals("habitatsMarine")) {
            return habitatsMarine;
        } else if (fldName.equals("speciesTerrestrial")) {
            return speciesTerrestrial;
        } else if (fldName.equals("speciesMarine")) {
            return speciesMarine;
        } else if (fldName.equals("totalReports")) {
            return totalReports;
        } else if (fldName.equals("countryCode")) {
            return countryCode;
        }

        // return super.getFieldValue(field);

        return null;
    }

    // return only 1 row
    @Override
    public boolean next() throws JRException {

        if (!fieldsFilled) {
            fillCalculatedFields();
            return true;
        }
        return false;
    }

    // as sparql is not exactly aggregated the same way
    private void fillCalculatedFields() {

        try {
            int speciesCount = 0;
            int habitatCount = 0;
            while (result.hasNext()) {
                BindingSet b = result.next();
                speciesCount = Integer.valueOf(b.getBinding("species").getValue().stringValue());
                habitatCount = Integer.valueOf(b.getBinding("habitat").getValue().stringValue());

                totalReports = totalReports + habitatCount + speciesCount;

                if (b.getBinding("ismarine").getValue().stringValue().equals("1")) {
                    habitatsMarine = habitatCount;
                    speciesMarine = speciesCount;
                } else {
                    habitatsTerrestrial = habitatCount;
                    speciesTerrestrial = speciesCount;

                }

            }
        } catch (QueryEvaluationException e) {
            e.printStackTrace();
        }

        fieldsFilled = true;
    }

}
