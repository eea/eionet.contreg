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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;

/**
 * 
 * Utility class to hold handy functions for formatting.
 * 
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public final class FormatUtils {
    /**
     * to prevent public instancing.
     */
    private FormatUtils() {
        // blank
    }

    /**
     * Returns object values for the given predicate in given langauges.
     * 
     * @param predicateUri predicate URI
     * @param subjectDTO SubjectDTO data object for subject
     * @param languages Set<String> language codes
     * @return String
     */
    public static String getObjectValuesForPredicate(String predicateUri, SubjectDTO subjectDTO, List<String> languages) {
        String result = "";

        if (subjectDTO.getPredicateCount() > 0) {

            Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
            if (objects != null && !objects.isEmpty()) {

                LinkedHashSet<ObjectDTO> distinctObjects = new LinkedHashSet<ObjectDTO>(objects);
                StringBuffer bufLiterals = new StringBuffer();
                StringBuffer bufNonLiterals = new StringBuffer();

                String resultFromHitSource = null;
                for (ObjectDTO objectDTO : distinctObjects) {

                    String objectString = objectDTO.getValue().trim();

                    // if the source of the object matches the search-hit source of the subject then
                    // remember the object value and break
                    if (subjectDTO.getHitSource() > 0 && objectDTO.getSourceHashSmart() == subjectDTO.getHitSource()
                            && !StringUtils.isBlank(objectString) && objectDTO.isLiteral()) {

                        resultFromHitSource = objectString;
                        break;
                    }

                    if (objectString.length() > 0) {

                        if (objectDTO.isLiteral()) {
                            if (languages.isEmpty() || languages.contains(objectDTO.getLanguage())) {
                                bufLiterals.append(bufLiterals.length() > 0 ? ", " : "").append(objectString);
                            }
                        } else {
                            bufNonLiterals.append(bufNonLiterals.length() > 0 ? ", " : "").append(objectString);
                        }
                    }
                }

                // if there was a value found that came from search-hit source then prefer that one as the result
                if (!StringUtils.isBlank(resultFromHitSource)) {
                    result = resultFromHitSource;
                } else {
                    result = bufLiterals.length() > 0 ? bufLiterals.toString() : bufNonLiterals.toString();
                }
            }
        }
        return result;
    }

}
