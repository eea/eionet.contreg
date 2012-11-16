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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dao.virtuoso;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;

/**
 *
 * @author Jaanus Heinlaid
 */
public class PredicateObjectsReader extends SPARQLResultSetBaseReader<ObjectDTO> {

    /** */
    protected Logger logger = Logger.getLogger(PredicateObjectsReader.class);

    /**  */
    public static final int PREDICATE_PAGE_SIZE = 10;
    /** */
    private List<String> acceptedLanguages;

    /** */
    private HashMap<String, HashSet<String>> distinctLiterals = new HashMap<String, HashSet<String>>();
    private HashSet<String> distinctResources = new HashSet<String>();

    /**
     * @param acceptedLanguages
     */
    public PredicateObjectsReader(List<String> acceptedLanguages) {
        this.acceptedLanguages = acceptedLanguages;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        Value objectValue = bindingSet.getValue("obj");
        Value objectLabelValue = bindingSet.getValue("objLabel");
        Value graphValue = bindingSet.getValue("g");

        if (objectValue != null) {

            String value = objectValue.stringValue();
            boolean isLiteral = objectValue instanceof Literal;
            if (!isLiteral) {
                value = objectValue.toString();
            }
            Literal literal = isLiteral ? (Literal) objectValue : null;
            String language = isLiteral ? literal.getLanguage() : null;

            if (!objectAlreadyAdded(value, isLiteral, language)) {

                URI datatype = isLiteral ? literal.getDatatype() : null;
                boolean isAnonymous = isLiteral ? false : objectValue instanceof BNode;

                ObjectDTO objectDTO = new ObjectDTO(value, language, isLiteral, isAnonymous, datatype);
                if (graphValue != null && graphValue instanceof Resource) {
                    objectDTO.setSourceUri(graphValue.stringValue());
                }

                if (objectLabelValue != null && objectLabelValue instanceof Literal) {

                    Literal labelLiteral = (Literal) objectLabelValue;
                    ObjectDTO labelObjectDTO = new ObjectDTO(labelLiteral.stringValue(), labelLiteral.getLanguage(), true, false);
                    objectDTO.setLabelObject(labelObjectDTO);
                }

                resultList.add(objectDTO);
                if (isLiteral) {
                    HashSet<String> valuesByLang = distinctLiterals.get(language);
                    if (valuesByLang == null) {
                        valuesByLang = new HashSet<String>();
                        distinctLiterals.put(language, valuesByLang);
                    }
                    valuesByLang.add(value);
                } else {
                    distinctResources.add(value);
                }
            }
        }
    }

    /**
     *
     * @param value
     * @param language
     * @return
     */
    private boolean objectAlreadyAdded(String value, boolean isLiteral, String language) {

        if (isLiteral) {
            HashSet<String> valuesByLang = distinctLiterals.get(language);
            return valuesByLang != null && valuesByLang.contains(value);
        } else {
            return distinctResources.contains(value);
        }
    }
}
