/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.staging.exp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.vocabulary.XMLSchema;

/**
 * Type definition ...
 *
 * @author jaanus
 */
public class ObjectTypes {

    /** */
    private static final LinkedHashMap<String, ObjectType> TYPES_BY_URI = load();

    /**
     *
     * @param uri
     * @return
     */
    public static ObjectType getByUri(String uri) {
        return TYPES_BY_URI.get(uri);
    }

    /**
     *
     * @return
     */
    public static Map<String, ObjectType> getMap() {
        return TYPES_BY_URI;
    }

    /**
     * @return
     */
    private static LinkedHashMap<String, ObjectType> load() {

        // FIXME load from a configuration file

        LinkedHashMap<String, ObjectType> result = new LinkedHashMap<String, ObjectType>();

        ObjectType qbObservation = new ObjectType("http://purl.org/linked-data/cube#Observation", "Data Cube observation");
        qbObservation.setDatasetColumn("dataset");
        qbObservation.setDatasetNamespace("http://scoreboard.lod2.eu/data/");
        qbObservation.setIdTemplate("<dataset>#A,<breakdown>,<unit>,<refArea>,<timePeriod>");
        qbObservation.setIdNamespace("http://scoreboard.lod2.eu/data/");

        // sdmx-dimension:refArea
        ObjectProperty property =
                new ObjectProperty("http://purl.org/linked-data/sdmx/2009/dimension#refArea", "SDMX reference area",
                        ObjectProperty.Range.RESOURCE);
        property.setValueTemplate("http://eurostat.linked-statistics.org/dic/geo#<value>");
        qbObservation.addProperty(property, "refArea");

        // sdmx-dimension:timePeriod
        property =
                new ObjectProperty("http://purl.org/linked-data/sdmx/2009/dimension#timePeriod", "SDMX time period",
                        ObjectProperty.Range.LITERAL);
        property.setDataType(XMLSchema.GYEAR.stringValue());
        qbObservation.addProperty(property, "timePeriod");

        // sdmx-dimension:freq
        property =
                new ObjectProperty("http://purl.org/linked-data/sdmx/2009/dimension#freq", "SDMX frequency",
                        ObjectProperty.Range.RESOURCE);
        property.setValueTemplate("http://purl.org/linked-data/sdmx/2009/code#freq-<value>");
        qbObservation.addProperty(property, "timePeriod");

        // qb:dataSet
        property = new ObjectProperty("http://purl.org/linked-data/cube#dataSet", "Data Cube dataset", ObjectProperty.Range.RESOURCE);
        property.setValueTemplate("http://scoreboard.lod2.eu/data/<value>");
        qbObservation.addProperty(property, "dataSet");

        // scb:breakdown
        property = new ObjectProperty("http://data.lod2.eu/scoreboard/properties/breakdown", "Scoreboard breakdown", ObjectProperty.Range.RESOURCE);
        property.setValueTemplate("http://scoreboard.lod2.eu/breakdowns/<value>");
        qbObservation.addProperty(property, "breakdown");

        // sdmx-attribute:unitMeasure
        property = new ObjectProperty("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure", "SMDX unit measure", ObjectProperty.Range.LITERAL);
        qbObservation.addProperty(property, "unit");

        // sdmx-measure:obsValue
        property = new ObjectProperty("http://purl.org/linked-data/sdmx/2009/measure#obsValue", "SMDX observed value", ObjectProperty.Range.LITERAL);
        property.setDataType(XMLSchema.DOUBLE.stringValue());
        qbObservation.addProperty(property, "value");

        result.put(qbObservation.getUri(), qbObservation);

        addDummyTypes(result, 3);

        return result;
    }

    /**
     *
     * @param map
     * @param noOfDummyTypes
     */
    private static void addDummyTypes(LinkedHashMap<String, ObjectType> map, int noOfDummyTypes) {

        for (int i = 1; i <= noOfDummyTypes; i++) {

            String typeLabel = "DummyType" + i;
            ObjectType type = new ObjectType("http://dummy.org/type#" + typeLabel, typeLabel);

            ArrayList<ObjectProperty> properties = new ArrayList<ObjectProperty>();
            ArrayList<String> propLabels = new ArrayList<String>();
            for (int j = 1; j <= 12; j++) {

                String propLabel = typeLabel + "_prop" + j;
                propLabels.add(propLabel);

                ObjectProperty property = new ObjectProperty("http://dummy.org/properties#"+ propLabel, propLabel, ObjectProperty.Range.LITERAL);
                type.addProperty(property, "col" + j);
                properties.add(property);
            }

            type.setDatasetColumn("col2");
            type.setIdTemplate("<col2>_<col3>_<col4>");

            map.put(type.getUri(), type);
        }
    }
}
