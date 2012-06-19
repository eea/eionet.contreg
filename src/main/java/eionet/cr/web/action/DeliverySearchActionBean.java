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
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.DeliveryFilterDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.DeliveryDTO;
import eionet.cr.dto.DeliveryFilterDTO;
import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.ApplicationCache;
import eionet.cr.web.util.CustomPaginatedList;
import eionet.cr.web.util.WebConstants;

/**
 *
 * @author altnyris
 *
 */
@UrlBinding("/deliverySearch.action")
public class DeliverySearchActionBean extends DisplaytagSearchActionBean {

    /** */
    private List<String> years;

    /** */
    private String obligation;
    private String locality;
    private String year;

    /** Search criteria to be stored for compiled dataset. */
    private String searchCriteria;

    /** Store delivery filters. */
    private List<DeliveryFilterDTO> deliveryFilters = new ArrayList<DeliveryFilterDTO>();

    /** Selected filter id. */
    private Long filterId;

    /** True, when dataset filter search is executed. */
    private Boolean datasetFilter;

    private CustomPaginatedList<DeliveryDTO> deliveries;

    private static final Map<String, String> COLUMNS = new HashMap<String, String>();
    static {
        COLUMNS.put("title", Predicates.RDFS_LABEL);
        COLUMNS.put("period", Predicates.ROD_PERIOD);
        COLUMNS.put("date", Predicates.ROD_RELEASED);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution init() throws DAOException {
        if (isUserLoggedIn()) {
            deliveryFilters = DAOFactory.get().getDao(DeliveryFilterDAO.class).getDeliveryFilters(getUserName());
        }

        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(obligation)) {
            sb.append(obligation);
            sb.append(WebConstants.FILTER_LABEL_SEPARATOR);
            sb.append(getObligationLabel(obligation));
        } else {
            sb.append(WebConstants.NOT_AVAILABLE);
        }
        sb.append(WebConstants.FILTER_SEPARATOR);
        if (StringUtils.isNotEmpty(locality)) {
            sb.append(locality);
            sb.append(WebConstants.FILTER_LABEL_SEPARATOR);
            sb.append(getLocalityLabel(locality));
        } else {
            sb.append(WebConstants.NOT_AVAILABLE);
        }
        sb.append(WebConstants.FILTER_SEPARATOR);
        if (StringUtils.isNotEmpty(year)) {
            sb.append(year);
        } else {
            sb.append(WebConstants.NOT_AVAILABLE);
        }
        searchCriteria = sb.toString();

        return new ForwardResolution("/pages/deliverySearch.jsp");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    public Resolution search() throws DAOException {

        deliveries =
                DAOFactory
                        .get()
                        .getDao(SearchDAO.class)
                        .searchDeliveries(obligation, locality, year, sort, PagingRequest.create(page),
                                new SortingRequest(COLUMNS.get(sort), SortOrder.parse(dir)));

        // Store search filter
        if (StringUtils.isNotEmpty(obligation) || StringUtils.isNotEmpty(locality) || StringUtils.isNotEmpty(year)) {
            DeliveryFilterDTO deliveryFilter = new DeliveryFilterDTO();
            deliveryFilter.setObligation(obligation);
            deliveryFilter.setLocality(locality);
            deliveryFilter.setYear(year);
            deliveryFilter.setUsername(getUserName());

            if (StringUtils.isNotEmpty(obligation)) {
                String obligationLabel = getObligationLabel(obligation);
                deliveryFilter.setObligationLabel(obligationLabel);
            }

            if (StringUtils.isNotEmpty(locality)) {
                String localityLabel = getLocalityLabel(locality);
                deliveryFilter.setLocalityLabel(localityLabel);
            }

            DAOFactory.get().getDao(DeliveryFilterDAO.class).saveDeliveryFilter(deliveryFilter);
        }

        return init();
    }

    /**
     * Returns label from the pair collection.
     *
     * @param obligationValue
     * @return
     */
    private String getObligationLabel(String obligationValue) {
        for (List<UriLabelPair> group : getInstrumentsObligations().values()) {
            for (UriLabelPair pair : group) {
                if (pair.getUri().equals(obligationValue)) {
                    return pair.getLabel();
                }
            }
        }
        return null;
    }

    /**
     * Returns label from the pair collection.
     *
     * @param localityValue
     * @return
     */
    private String getLocalityLabel(String localityValue) {
        for (ObjectLabelPair pair : getLocalities()) {
            if (pair.getLeft().equals(localityValue)) {
                return pair.getRight();
            }
        }
        return null;
    }

    /**
     * Action for searching deliveries using a user's stored filter.
     *
     * @return
     * @throws DAOException
     */
    public Resolution filterSearch() throws DAOException {
        DeliveryFilterDTO filter = DAOFactory.get().getDao(DeliveryFilterDAO.class).getDeliveryFilte(filterId);

        obligation = filter.getObligation();
        locality = filter.getLocality();
        year = filter.getYear();

        deliveries =
                DAOFactory
                        .get()
                        .getDao(SearchDAO.class)
                        .searchDeliveries(obligation, locality, year, sort, PagingRequest.create(page),
                                new SortingRequest(COLUMNS.get(sort), SortOrder.parse(dir)));

        return init();
    }

    /**
     * Action for searching deliveries from a compiled dataset's stored filter.
     *
     * @return
     * @throws DAOException
     */
    public Resolution datasetFilterSearch() throws DAOException {
        deliveries =
                DAOFactory
                        .get()
                        .getDao(SearchDAO.class)
                        .searchDeliveries(obligation, locality, year, sort, PagingRequest.create(page),
                                new SortingRequest(COLUMNS.get(sort), SortOrder.parse(dir)));
        logger.debug("found deliveries: " + deliveries.getFullListSize() + "; " + datasetFilter);
        return init();
    }

    /**
     * @return the instrumentsObligations
     */
    public Map<UriLabelPair, List<UriLabelPair>> getInstrumentsObligations() {
        return ApplicationCache.getDeliverySearchPicklist();
    }

    /**
     * @return the countries
     */
    public Collection<ObjectLabelPair> getLocalities() {
        return ApplicationCache.getLocalities();
    }

    /**
     * @return the years
     */
    public List<String> getYears() {

        if (years == null) {
            years = new ArrayList<String>();
            int curYear = Calendar.getInstance().get(Calendar.YEAR);
            int earliestYear = 1990;
            for (int i = curYear; i >= earliestYear; i--)
                years.add(String.valueOf(i));
        }

        return years;
    }

    /**
     * @return the obligation
     */
    public String getObligation() {
        return obligation;
    }

    /**
     * @return the locality
     */
    public String getLocality() {
        return locality;
    }

    /**
     * @return the year
     */
    public String getYear() {
        return year;
    }

    /**
     * @param obligation the obligation to set
     */
    public void setObligation(String obligation) {
        this.obligation = obligation;
    }

    /**
     * @param locality the locality to set
     */
    public void setLocality(String locality) {
        this.locality = locality;
    }

    /**
     * @param year the year to set
     */
    public void setYear(String year) {
        this.year = year;
    }

    public CustomPaginatedList<DeliveryDTO> getDeliveries() {
        return deliveries;
    }

    /**
     * @return the deliveryFilters
     */
    public List<DeliveryFilterDTO> getDeliveryFilters() {
        return deliveryFilters;
    }

    /**
     * @return the filterId
     */
    public Long getFilterId() {
        return filterId;
    }

    /**
     * @param filterId the filterId to set
     */
    public void setFilterId(Long filterId) {
        this.filterId = filterId;
    }

    /**
     * @return the searchCriteria
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * @param searchCriteria the searchCriteria to set
     */
    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    /**
     * @return the datasetFilter
     */
    public Boolean getDatasetFilter() {
        return datasetFilter;
    }

    /**
     * @param datasetFilter the datasetFilter to set
     */
    public void setDatasetFilter(Boolean datasetFilter) {
        this.datasetFilter = datasetFilter;
    }

}
