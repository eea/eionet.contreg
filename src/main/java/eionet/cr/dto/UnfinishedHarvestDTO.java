package eionet.cr.dto;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UnfinishedHarvestDTO {

	private long source;
	private long genTime;
	/**
	 * @return the source
	 */
	public long getSource() {
		return source;
	}
	/**
	 * @param source the source to set
	 */
	public void setSource(long source) {
		this.source = source;
	}
	/**
	 * @return the genTime
	 */
	public long getGenTime() {
		return genTime;
	}
	/**
	 * @param genTime the genTime to set
	 */
	public void setGenTime(long genTime) {
		this.genTime = genTime;
	}
	
	/**
	 * 
	 * @param source
	 * @param genTime
	 * @return
	 */
	public static UnfinishedHarvestDTO create(long source, long genTime){
		UnfinishedHarvestDTO dto = new UnfinishedHarvestDTO();
		dto.setSource(source);
		dto.setGenTime(genTime);
		return dto;
	}
}
