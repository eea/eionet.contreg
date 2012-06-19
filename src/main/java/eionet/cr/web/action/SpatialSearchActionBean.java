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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/spatialSearch.action")
public class SpatialSearchActionBean extends AbstractSearchActionBean<SubjectDTO> {

    /** */
    private static Log logger = LogFactory.getLog(SpatialSearchActionBean.class);

    /** */
    public static final String NO_KMLLINKS = "no_kmllinks";

    /** */
    private String bbox;

    /** */
    private String source;

    /** */
    private Double latS, latN, longW, longE;

    private List<String> sources;

    /** */
    private String contextUrl;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution onEventUnspecified() throws DAOException {

        if (this.bbox != null)
            return doKml();
        else
            return new ForwardResolution("/pages/spatialSearch.jsp");
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    private Resolution doKml() throws DAOException {

        logger.debug("KML requested, BBOX = " + this.bbox);

        String[] ltudes = this.bbox.split(",");
        if (ltudes != null && ltudes.length == 4) {

            longW = Util.toDouble(ltudes[0].trim());
            latS = Util.toDouble(ltudes[1].trim());
            longE = Util.toDouble(ltudes[2].trim());
            latN = Util.toDouble(ltudes[3].trim());

            SortingRequest sortingRequest = new SortingRequest(Predicates.RDFS_LABEL, SortOrder.ASCENDING);
            sortingRequest.setSortByPredicateObjectHash(true);
            Pair<Integer, List<SubjectDTO>> resultPair =
                    DAOFactory.get().getDao(SearchDAO.class)
                            .searchBySpatialBox(createBBOX(), source, PagingRequest.create(1, 25), sortingRequest, true);
            resultList = resultPair.getRight();
        }

        try {
            getContext().getRequest().setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString(), e);
        }
        getContext().getResponse().setHeader("Content-Disposition", "attachment; filename=placemarks.kml");

        return new ForwardResolution("/pages/placemarks.jsp");
    }

    /**
     *
     * @return
     */
    private Collection<SubjectDTO> filterPlacemarks(Collection<SubjectDTO> placemarks) {

        if (placemarks == null || placemarks.isEmpty())
            return placemarks;

        double delta = (longE - longW) / 100;
        delta = delta * delta;

        Collection<SubjectDTO> result = new ArrayList<SubjectDTO>();
        for (SubjectDTO subject : placemarks) {

            String latit = subject.getObjectValue(Predicates.WGS_LAT);
            String longit = subject.getObjectValue(Predicates.WGS_LONG);
            if (!StringUtils.isBlank(latit) && !StringUtils.isBlank(longit)) {
                if (!isTooClose(Double.parseDouble(latit.trim()), Double.parseDouble(longit.trim()), delta, result)) {
                    result.add(subject);
                }
            }
        }

        return result;
    }

    /**
     *
     * @param latit
     * @param longit
     * @param delta
     * @return
     */
    private static boolean isTooClose(double latit, double longit, double delta, Collection<SubjectDTO> coll) {

        for (SubjectDTO subject : coll) {

            double subjLat = getLatitude(subject);
            double subjLong = getLongitude(subject);

            double diffLat = (subjLat - latit) * (subjLat - latit);
            double diffLong = (subjLong - longit) * (subjLong - longit);
            double diff = diffLat + diffLong;
            if (diff < delta) {
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param subject
     * @return
     */
    private static double getLatitude(SubjectDTO subject) {
        return Double.parseDouble(subject.getObjectValue(Predicates.WGS_LAT).trim());
    }

    /**
     *
     * @param subject
     * @return
     */
    private static double getLongitude(SubjectDTO subject) {
        return Double.parseDouble(subject.getObjectValue(Predicates.WGS_LONG).trim());
    }

    /**
     *
     * @return
     */
    public Resolution googleEarthIntro() {

        return new ForwardResolution("/pages/googleEarthIntro.jsp");
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution kmlLinks() throws DAOException {

        sources = factory.getDao(HelperDAO.class).getSpatialSources();
        if (sources.isEmpty()) {
            addSystemMessage("No spatial objects currently found!");
            return new ForwardResolution("/pages/googleEarthIntro.jsp");
        } else {
            try {
                getContext().getRequest().setCharacterEncoding("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString(), e);
            }
            getContext().getResponse().setHeader("Content-Disposition", "attachment; filename=kmllinks.kml");
            return new ForwardResolution("/pages/kmllinks.jsp");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    public Resolution search() throws DAOException {

        BBOX box = createBBOX();
        if (!box.isUndefined()) {

            Pair<Integer, List<SubjectDTO>> resultPair =
                    DAOFactory
                            .get()
                            .getDao(SearchDAO.class)
                            .searchBySpatialBox(createBBOX(), source, PagingRequest.create(getPageN()),
                                    new SortingRequest(getSortP(), SortOrder.parse(getSortO())), false);
            resultList = resultPair.getRight();
            matchCount = resultPair.getLeft();
        }

        return new ForwardResolution("/pages/spatialSearch.jsp");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
     */
    public List<SearchResultColumn> getColumns() throws DAOException {

        ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

        SubjectPredicateColumn col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.RDF_TYPE);
        col.setTitle("Type");
        col.setSortable(true);
        list.add(col);

        col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.RDFS_LABEL);
        col.setTitle("Title");
        col.setSortable(true);
        list.add(col);

        col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.WGS_LAT);
        col.setTitle("Latitude");
        col.setSortable(true);
        list.add(col);

        col = new SubjectPredicateColumn();
        col.setPredicateUri(Predicates.WGS_LONG);
        col.setTitle("Longitude");
        col.setSortable(true);
        list.add(col);

        return list;
    }

    /**
     *
     * @return
     */
    private BBOX createBBOX() {

        BBOX box = new BBOX();
        box.setLatitudeSouth(latS);
        box.setLatitudeNorth(latN);
        box.setLongitudeWest(longW);
        box.setLongitudeEast(longE);

        return box;
    }

    /**
     * @return the latS
     */
    public Double getLatS() {
        return latS;
    }

    /**
     * @param latS the latS to set
     */
    public void setLatS(Double lat1) {
        this.latS = lat1;
    }

    /**
     * @return the latN
     */
    public Double getLatN() {
        return latN;
    }

    /**
     * @param latN the latN to set
     */
    public void setLatN(Double lat2) {
        this.latN = lat2;
    }

    /**
     * @return the longW
     */
    public Double getLongW() {
        return longW;
    }

    /**
     * @param longW the longW to set
     */
    public void setLongW(Double long1) {
        this.longW = long1;
    }

    /**
     * @return the longE
     */
    public Double getLongE() {
        return longE;
    }

    /**
     * @param longE the longE to set
     */
    public void setLongE(Double long2) {
        this.longE = long2;
    }

    /**
     *
     * @param bbox
     */
    public void setBBOX(String bbox) {
        this.bbox = bbox;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the contextUrl
     */
    public String getContextUrl() {

        if (contextUrl == null) {
            HttpServletRequest request = getContext().getRequest();

            StringBuffer buf = new StringBuffer(request.getScheme()).append("://").append(request.getServerName());
            int port = request.getServerPort();
            if (port > 0 && !(port == 80 && request.getScheme().equalsIgnoreCase("http"))) {
                buf.append(":").append(port);
            }
            String contextPath = request.getContextPath();
            if (contextPath != null) {
                buf.append(contextPath.trim());
            }

            contextUrl = buf.toString();
        }

        return contextUrl;
    }

    /**
     * @return the sources
     */
    public List<String> getSources() {
        return sources;
    }
}
