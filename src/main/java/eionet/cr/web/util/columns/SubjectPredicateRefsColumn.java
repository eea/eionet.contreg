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
package eionet.cr.web.util.columns;

import java.util.Collection;
import java.util.List;

import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

//same as SubjectPredicateColumn class exept Title is shown as combination of Uri ending + actual label.

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class SubjectPredicateRefsColumn extends SearchResultColumn {

    /** */
    private String predicateUri;

    /** */
    private List<String> languages;

    /**
     *
     */
    public SubjectPredicateRefsColumn() {
        // blank
    }

    /**
     * @param title
     * @param isSortable
     * @param predicateUri
     */
    public SubjectPredicateRefsColumn(String title, boolean isSortable, String predicateUri) {

        super(title, isSortable);
        this.predicateUri = predicateUri;
    }

    /**
     * Constructor.
     * 
     * @param title
     * @param isSortable
     * @param predicateUri
     * @param actionRequestParameter
     */
    public SubjectPredicateRefsColumn(String title, boolean isSortable, String predicateUri, String actionRequestParameter) {
        super(title, isSortable);
        this.predicateUri = predicateUri;
        setActionRequestParameter(actionRequestParameter);
    }

    /**
     * @return the predicateUri
     */
    public String getPredicateUri() {
        return predicateUri;
    }

    /**
     * @param predicateUri the predicateUri to set
     */
    public void setPredicateUri(String predicateUri) {
        this.predicateUri = predicateUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.web.util.columns.SearchResultColumn#format(java.lang.Object)
     * 
     * Gets the collection of objects matching to the given predicate in the given subject. Formats the given collection to
     * comma-separated string. For literal objects, simply the value of the literal will be used. For resource objects, clickable
     * factsheet links will be created.
     */
    public String format(Object object) {

        String result = null;
        if (object != null && object instanceof SubjectDTO && predicateUri != null) {

            SubjectDTO subjectDTO = (SubjectDTO) object;
            Collection<ObjectDTO> objects = subjectDTO.getObjectsForSearchResultsDisplay(predicateUri, getLanguages());

            if (predicateUri.equals(Predicates.RDFS_LABEL)) {

                if (objects.isEmpty()) {
                    result = URIUtil.extractURILabel(subjectDTO.getUri(), SubjectDTO.NO_LABEL);
                } else {
                    result = objectValuesToCSV(objects);
                }
                String uriLabel = URIUtil.extractURILabel(subjectDTO.getUri(), SubjectDTO.NO_LABEL);
                result = uriLabel + " (" + result + ")";
                result = buildFactsheetLink(subjectDTO.getUri(), result);
            } else if (!objects.isEmpty()) {

                StringBuffer buf = new StringBuffer();
                for (ObjectDTO o : objects) {

                    if (buf.length() > 0) {
                        buf.append(", ");
                    }

                    if (o.isLiteral()) {
                        buf.append(o.getValue());
                    } else {
                        String label = o.getDerviedLiteralValue();
                        if (label == null) {
                            label = URIUtil.extractURILabel(o.getValue(), SubjectDTO.NO_LABEL);
                        }
                        buf.append(buildFactsheetLink(o.getValue(), label));
                    }
                }
                result = buf.toString();
            }
        }

        return StringUtils.isBlank(result) ? "&nbsp;" : result;
    }

    /**
     * 
     * @param objects
     * @return
     */
    private String objectValuesToCSV(Collection<ObjectDTO> objects) {

        StringBuffer buf = new StringBuffer();
        for (ObjectDTO object : objects) {
            buf.append(buf.length() > 0 ? ", " : "").append(object.getValue());
        }
        return buf.toString();
    }

    /**
     * 
     * @param uri
     * @param label
     * @return
     */
    private String buildFactsheetLink(String uri, String label) {

        String factsheetUrlBinding = FactsheetActionBean.class.getAnnotation(UrlBinding.class).value();
        int i = factsheetUrlBinding.lastIndexOf("/");

        StringBuffer href = new StringBuffer(i >= 0 ? factsheetUrlBinding.substring(i + 1) : factsheetUrlBinding).append("?");
        href.append("uri=").append(Util.urlEncode(uri));

        return new StringBuffer("<a href=\"").append(href).append("\">").append(label).append("</a>").toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.web.util.search.SearchResultColumn#getSortParamValue()
     */
    public String getSortParamValue() {
        return predicateUri;
    }

    /**
     * 
     * @return
     */
    private List<String> getLanguages() {

        if (languages == null) {
            if (actionBean != null) {
                languages = actionBean.getAcceptedLanguages();
            }
        }

        return languages;
    }
}
