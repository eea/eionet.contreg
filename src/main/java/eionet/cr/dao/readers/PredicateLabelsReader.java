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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao.readers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.FactsheetDTO;
import eionet.cr.util.URIUtil;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PredicateLabelsReader extends SPARQLResultSetBaseReader {

    /** */
    private List<String> acceptedLanguages;

    /** */
    private Map<String, Literal> predicateLiterals = new HashMap<String, Literal>();

    /**
     * @param acceptedLanguages
     */
    public PredicateLabelsReader(List<String> acceptedLanguages) {
        this.acceptedLanguages = acceptedLanguages;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) {

        String predicateUri = getStringValue(bindingSet, "pred");

        Value value = bindingSet.getValue("label");
        if (value instanceof Literal) {

            Literal literal = (Literal) value;
            if (acceptedLanguages == null || acceptedLanguages.isEmpty()) {
                predicateLiterals.put(predicateUri, literal);
            } else {
                String language = unrefineLanguage(literal.getLanguage());
                int languageIndex = acceptedLanguages.indexOf(language);
                if (languageIndex >= 0) {
                    Literal currentLiteral = predicateLiterals.get(predicateUri);
                    if (currentLiteral == null || languageIndex < acceptedLanguages.indexOf(currentLiteral.getLanguage())) {
                        predicateLiterals.put(predicateUri, literal);
                    }
                }
            }
        }
    }

    /**
     * Un-refines the given language code (i.e. "en-GB" becomes "en", "en_us" becomes "en", etc).
     *
     * @param literal
     * @return
     */
    private String unrefineLanguage(String language) {

        return language == null ? "" : StringUtils.split(language, "-_")[0];
    }

    /**
     *
     * @param factsheetDTO
     */
    public void fillPredicateLabels(FactsheetDTO factsheetDTO) {

        for (String predicateUri : factsheetDTO.getPredicateUris()) {

            Literal literal = predicateLiterals.get(predicateUri);
            String label = literal == null ? null : literal.getLabel();
            if (StringUtils.isBlank(label)) {
                label = URIUtil.extractURILabel(predicateUri, predicateUri);
            }
            factsheetDTO.addPredicateLabel(predicateUri, label);
        }
    }
}
