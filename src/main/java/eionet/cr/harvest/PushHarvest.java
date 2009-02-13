package eionet.cr.harvest;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import eionet.cr.harvest.util.ReaderBasedARPSource;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PushHarvest extends Harvest{
	
	/** */
	protected String content = null;

	/**
	 * 
	 * @param sourceUri
	 */
	public PushHarvest(String content, String sourceUri) {
		super(sourceUri);
		this.content = content;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.harvest.Harvest#doExecute()
	 */
	protected void doExecute() throws HarvestException {
		
		if (content==null || content.trim().length()==0)
			throw new HarvestException("No content supplied for push-harvesting from url [" + sourceUrlString + "]");
		
		Reader stringReader = null;
		try{
			stringReader = new StringReader(content);
			harvest(new ReaderBasedARPSource(stringReader));
		}
		finally{
			if (stringReader!=null){
				try{stringReader.close();}catch (IOException e){}
			}
		}
	}
}
