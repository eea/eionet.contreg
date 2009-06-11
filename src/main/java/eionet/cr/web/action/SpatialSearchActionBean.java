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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.common.Predicates;
import eionet.cr.search.CustomSearch;
import eionet.cr.search.SearchException;
import eionet.cr.search.SpatialSearch;
import eionet.cr.search.SpatialSourcesSearch;
import eionet.cr.search.util.BBOX;
import eionet.cr.search.util.UriLabelPair;
import eionet.cr.util.Util;
import eionet.cr.web.util.search.PredicateBasedColumn;
import eionet.cr.web.util.search.SearchResultColumn;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/spatialSearch.action")
public class SpatialSearchActionBean extends AbstractSearchActionBean {

	/** */
	private static Log logger = LogFactory.getLog(SpatialSearchActionBean.class);
	
	/** */
	public static final String NO_KMLLINKS = "no_kmllinks";
	
	/** */
	private String BBOX;
	
	/** */
	private String source;
	
	/** */
	private Double latS,latN,longW,longE;
	
	private List<String> sources;
	
	/** */
	private String contextUrl;
	
	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	@DefaultHandler
	public Resolution onEventUnspecified() throws SearchException{
		
		if (BBOX!=null)
			return doKml();
		else
			return new ForwardResolution("/pages/spatialSearch.jsp");
	}
	
	/**
	 * 
	 * @param bbox
	 * @param source
	 * @return
	 * @throws SearchException 
	 */
	private Resolution doKml() throws SearchException{
		
		logger.debug("kml requested, BBXO = " + BBOX);
		
		String[] ltudes = BBOX.split(",");
		if (ltudes!=null && ltudes.length==4){
			
			longW = Util.toDouble(ltudes[0].trim());
			latS = Util.toDouble(ltudes[1].trim());
			longE = Util.toDouble(ltudes[2].trim());
			latN = Util.toDouble(ltudes[3].trim());
			
			SpatialSearch spatialSearch = new SpatialSearch(createBBOX(), source);
			spatialSearch.setNoLimit();
			spatialSearch.execute();
			resultList = spatialSearch.getResultList();
		}
		
		try {
			getContext().getRequest().setCharacterEncoding("UTF-8");
		}
		catch (UnsupportedEncodingException e){
			throw new RuntimeException(e.toString(), e);
		}
		getContext().getResponse().setHeader("Content-Disposition", "attachment; filename=placemarks.kml");
		
		return new ForwardResolution("/pages/placemarks.jsp");
	}
	
	/**
	 * 
	 * @return
	 */
	public Resolution googleEarthIntro(){
		
		return new ForwardResolution("/pages/googleEarthIntro.jsp");
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	public Resolution kmlLinks() throws SearchException {
		
		sources = SpatialSourcesSearch.execute();
		if (sources.isEmpty()){
			showMessage("No spatial objects currently found!");
			return new ForwardResolution("/pages/googleEarthIntro.jsp");
		}
		else{
			try {
				getContext().getRequest().setCharacterEncoding("UTF-8");
			}
			catch (UnsupportedEncodingException e){
				throw new RuntimeException(e.toString(), e);
			}
			getContext().getResponse().setHeader("Content-Disposition", "attachment; filename=kmllinks.kml");
			return new ForwardResolution("/pages/kmllinks.jsp");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {
		
		BBOX bbox = createBBOX();
		if (!bbox.isUndefined()){
			
			SpatialSearch spatialSearch = new SpatialSearch(bbox, source);
			spatialSearch.setPageNumber(getPageN());
			spatialSearch.setSorting(getSortP(), getSortO());

			spatialSearch.execute();
			
			resultList = spatialSearch.getResultList();
    		matchCount = spatialSearch.getTotalMatchCount();
		}
		
		return new ForwardResolution("/pages/spatialSearch.jsp");
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#getColumns()
	 */
	public List<SearchResultColumn> getColumns() throws SearchException {
		
		ArrayList<SearchResultColumn> list = new ArrayList<SearchResultColumn>();

		PredicateBasedColumn col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.RDF_TYPE);
		col.setTitle("Type");
		col.setSortable(true);
		list.add(col);

		col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.RDFS_LABEL);
		col.setTitle("Title");
		col.setSortable(true);
		list.add(col);
		
		col = new PredicateBasedColumn();
		col.setPredicateUri(Predicates.WGS_LAT);
		col.setTitle("Latitude");
		col.setSortable(true);
		list.add(col);

		col = new PredicateBasedColumn();
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
	private BBOX createBBOX(){
		
		BBOX bbox = new BBOX();
		bbox.setLatitudeSouth(latS);
		bbox.setLatitudeNorth(latN);
		bbox.setLongitudeWest(longW);
		bbox.setLongitudeEast(longE);
		
		return bbox;
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
	 * @param bbox the bBOX to set
	 */
	public void setBBOX(String bbox) {
		BBOX = bbox;
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
		
		if (contextUrl==null){
			HttpServletRequest request = getContext().getRequest();
			
			StringBuffer buf = new StringBuffer(request.getScheme()).append("://").append(request.getServerName());
			int port = request.getServerPort();
			if (port>0 && !(port==80 && request.getScheme().equalsIgnoreCase("http"))){
				buf.append(":").append(port);
			}
			String contextPath = request.getContextPath();
			if (contextPath!=null){
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
