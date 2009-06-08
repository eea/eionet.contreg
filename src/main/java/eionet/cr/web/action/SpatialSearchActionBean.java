package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import eionet.cr.search.util.CoordinateBox;
import eionet.cr.search.util.UriLabelPair;
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
	private CoordinateBox box;
	
	/** */
	private Double lat1,lat2,long1,long2;
	
	@DefaultHandler
	public Resolution init(){
		return new ForwardResolution("/pages/spatialSearch.jsp");
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	public Resolution search() throws SearchException {
		
		if (getBox()!=null && !getBox().isUndefined()){
			
			SpatialSearch spatialSearch = new SpatialSearch(getBox());
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
	private CoordinateBox getBox(){
		
		if (box==null){
			box = new CoordinateBox();
			box.setLowerLatitude(lat1);
			box.setUpperLatitude(lat2);
			box.setLowerLongitude(long1);
			box.setUpperLongitude(long2);
		}
		
		return box;
	}

	/**
	 * @return the lat1
	 */
	public Double getLat1() {
		return lat1;
	}

	/**
	 * @param lat1 the lat1 to set
	 */
	public void setLat1(Double lat1) {
		this.lat1 = lat1;
	}

	/**
	 * @return the lat2
	 */
	public Double getLat2() {
		return lat2;
	}

	/**
	 * @param lat2 the lat2 to set
	 */
	public void setLat2(Double lat2) {
		this.lat2 = lat2;
	}

	/**
	 * @return the long1
	 */
	public Double getLong1() {
		return long1;
	}

	/**
	 * @param long1 the long1 to set
	 */
	public void setLong1(Double long1) {
		this.long1 = long1;
	}

	/**
	 * @return the long2
	 */
	public Double getLong2() {
		return long2;
	}

	/**
	 * @param long2 the long2 to set
	 */
	public void setLong2(Double long2) {
		this.long2 = long2;
	}
}
