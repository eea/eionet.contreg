package eionet.cr.search.util;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class CoordinateBox {

	/** */
	private Double lowerLatitude;
	private Double upperLatitude;
	private Double lowerLongitude;
	private Double upperLongitude;
	
	/**
	 * @return the lowerLatitude
	 */
	public Double getLowerLatitude() {
		return lowerLatitude;
	}
	/**
	 * @param lowerLatitude the lowerLatitude to set
	 */
	public void setLowerLatitude(Double lowerLat) {
		this.lowerLatitude = lowerLat;
	}
	/**
	 * @return the upperLatitude
	 */
	public Double getUpperLatitude() {
		return upperLatitude;
	}
	/**
	 * @param upperLatitude the upperLatitude to set
	 */
	public void setUpperLatitude(Double upperLat) {
		this.upperLatitude = upperLat;
	}
	/**
	 * @return the lowerLongitude
	 */
	public Double getLowerLongitude() {
		return lowerLongitude;
	}
	/**
	 * @param lowerLongitude the lowerLongitude to set
	 */
	public void setLowerLongitude(Double lowerLong) {
		this.lowerLongitude = lowerLong;
	}
	/**
	 * @return the upperLongitude
	 */
	public Double getUpperLongitude() {
		return upperLongitude;
	}
	/**
	 * @param upperLongitude the upperLongitude to set
	 */
	public void setUpperLongitude(Double upperLong) {
		this.upperLongitude = upperLong;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isUndefined(){
		return lowerLatitude==null && upperLatitude==null && lowerLongitude==null && upperLongitude==null;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasLatitude(){
		return lowerLatitude!=null || upperLatitude!=null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean hasLongitude(){
		return lowerLongitude!=null || upperLongitude!=null;
	}
}
