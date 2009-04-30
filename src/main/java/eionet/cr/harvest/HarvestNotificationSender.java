package eionet.cr.harvest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import eionet.cr.util.EMailSender;
import eionet.cr.util.Util;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestNotificationSender {
	
	/**
	 * 
	 */
	public HarvestNotificationSender(){
	}

	/**
	 * 
	 * @param harvest
	 * @throws HarvestException
	 */
	protected void notifyMessages(Harvest harvest) throws HarvestException{
		
		Throwable fatal = harvest.getFatalError();
		List<Throwable> errors = harvest.getErrors();
		if (fatal!=null || (errors!=null && !errors.isEmpty())){

			StringBuffer buf = new StringBuffer("The following error(s) happend while harvesting\n").
			append(harvest.getSourceUrlString());
			
			if (fatal!=null)
				buf.append("\n\n---\n\n").append(Util.getStackTrace(fatal));
			
			for (Iterator<Throwable> iter=errors.iterator(); iter.hasNext();){
				buf.append("\n\n---\n\n").append(Util.getStackTrace(iter.next()));
			}
			
			try {
				EMailSender.sendToSysAdmin("Error(s) when harvesting " + harvest.sourceUrlString, buf.toString());
			}
			catch (AddressException e) {
				throw new HarvestException(e.toString(), e);
			}
			catch (MessagingException e) {
				throw new HarvestException(e.toString(), e);
			}
		}
	}
	
	/**
	 * 
	 * @param t
	 * @param harvest
	 * @throws HarvestException
	 */
	protected void notifyMessagesAfterHarvest(List<Throwable> throwables, Harvest harvest) throws HarvestException{
		
		if (throwables==null || throwables.isEmpty())
			return;
		
		StringBuffer buf = new StringBuffer("The following error(s) happened *after* harvesting").
		append(harvest.getSourceUrlString());
		
		for (Iterator<Throwable> iter=throwables.iterator(); iter.hasNext();){
			buf.append("\n\n---\n\n").append(Util.getStackTrace(iter.next()));
		}
		
		try {
			EMailSender.sendToSysAdmin("Error(s) *after* harvesting " + harvest.sourceUrlString, buf.toString());
		}
		catch (AddressException e) {
			throw new HarvestException(e.toString(), e);
		}
		catch (MessagingException e) {
			throw new HarvestException(e.toString(), e);
		}
	}
}
