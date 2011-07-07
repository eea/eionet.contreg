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
package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/simpleSearch.action")
public class SimpleSearchActionBean extends AbstractSearchActionBean<SubjectDTO> {

    /** */
    private String searchExpression;
    private boolean isUri;
    private String simpleFilter;
    private boolean exactMatch;

    /**
     *
     * @return ForwardResolution
     */
    @DefaultHandler
    public Resolution init() {
        return new ForwardResolution("/pages/simpleSearch.jsp");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    public Resolution search() throws DAOException {

        SearchExpression searchExpression = new SearchExpression(this.searchExpression);
        FreeTextSearchHelper.FilterType filterType = FreeTextSearchHelper.FilterType.ANY_OBJECT;

        if (!searchExpression.isEmpty()) {

            if (simpleFilter != null) {

                if (simpleFilter.equals("anyObject")) {
                    filterType = FreeTextSearchHelper.FilterType.ANY_OBJECT;
                } else if (simpleFilter.equals("anyFile")) {
                    filterType = FreeTextSearchHelper.FilterType.ANY_FILE;
                } else if (simpleFilter.equals("texts")) {
                    filterType = FreeTextSearchHelper.FilterType.TEXTS;
                } else if (simpleFilter.equals("datasets")) {
                    filterType = FreeTextSearchHelper.FilterType.DATASETS;
                } else if (simpleFilter.equals("images")) {
                    filterType = FreeTextSearchHelper.FilterType.IMAGES;
                } else if (simpleFilter.equals("exactMatch")) {
                    exactMatch = true;
                }
            }

            if (searchExpression.isUri()) {

                this.isUri = true;
                SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(searchExpression.toString());
                if (subject != null) {
                    resultList = Collections.singleton(subject);
                    matchCount = 1;
                }
            }

            if (resultList == null || resultList.size() == 0) {
                Pair<Integer, List<SubjectDTO>> result =
                        DAOFactory
                                .get()
                                .getDao(SearchDAO.class)
                                .searchByFreeText(searchExpression, filterType, exactMatch, PagingRequest.create(getPageN()),
                                        new SortingRequest(getSortP(), SortOrder.parse(getSortO())));

                resultList = result.getRight();
                matchCount = result.getLeft();

                int exactRowCountLimit = DAOFactory.get().getDao(SearchDAO.class).getExactRowCountLimit();
                exactCount = exactRowCountLimit <= 0 || matchCount <= exactRowCountLimit;
            }
        }

        return new ForwardResolution("/pages/simpleSearch.jsp");
    }

    @ValidationMethod(on = "search")
    public void validateSearch(ValidationErrors errors) {
        if (this.searchExpression == null || this.searchExpression.equals("")) {
            addCautionMessage(getBundle().getString("search.field.empty"));
        }
    }

    /**
     * @return the searchExpression
     */
    public String getSearchExpression() {
        return searchExpression;
    }

    /**
     * @param searchExpression
     *            the searchExpression to set
     */
    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    public List<SearchResultColumn> getColumns() {

        ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

        SubjectPredicateColumn col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.RDF_TYPE);
        col.setTitle("Type");
        col.setSortable(true);
        list.add(col);

        col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.RDFS_LABEL);
        col.setTitle("Label");
        col.setSortable(true);
        list.add(col);

        SubjectLastModifiedColumn col2 = new SubjectLastModifiedColumn();
        col2.setTitle("Date");
        col2.setSortable(true);
        list.add(col2);

        return list;
    }

    /**
     * @return the isUri
     */
    public boolean isUri() {
        return isUri;
    }

    /**
     * @param isUri
     *            the isUri to set
     */
    public void setUri(boolean isUri) {
        this.isUri = isUri;
    }

    public String getSimpleFilter() {
        return simpleFilter;
    }

    public void setSimpleFilter(String simpleFilter) {
        this.simpleFilter = simpleFilter;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    public void setExactMatch(boolean exactMatch) {
        this.exactMatch = exactMatch;
    }
}
