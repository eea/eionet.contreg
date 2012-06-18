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
package eionet.cr.common;

import java.util.Iterator;
import java.util.LinkedHashSet;

import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class LabelPredicates {

    /** */
    private static final LabelPredicates instance = new LabelPredicates();

    /** */
    private LinkedHashSet<String> predicates;

    /** */
    private String[] predicateHashes;

    /** */
    private String commaSeparatedHashes;

    /**
     *
     */
    private LabelPredicates() {

        load();

        int i = 0;
        StringBuffer buf = new StringBuffer();

        predicateHashes = new String[predicates.size()];
        for (Iterator<String> iter = predicates.iterator(); iter.hasNext(); i++) {
            predicateHashes[i] = String.valueOf(Hashes.spoHash(iter.next()));
            if (i > 0)
                buf.append(",");
            buf.append(predicateHashes[i]);
        }

        commaSeparatedHashes = buf.toString();
    }

    /**
     *
     */
    private void load() {

        // TODO - real loading must be done from some configuration
        predicates = new LinkedHashSet<String>();
        predicates.add(Predicates.RDFS_LABEL);
        predicates.add(Predicates.SKOS_PREF_LABEL);
        predicates.add(Predicates.DC_TITLE);
    }

    /**
     * 
     * @return
     */
    public static String getCommaSeparatedHashes() {
        return LabelPredicates.instance.commaSeparatedHashes;
    }
}
