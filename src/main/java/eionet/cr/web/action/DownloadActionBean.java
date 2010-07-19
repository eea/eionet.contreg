package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.DownloadFileDTO;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tietoenator.com">Jaak Kapten</a>
 *
 */
@UrlBinding("/download.action")

public class DownloadActionBean extends AbstractActionBean {

	@DefaultHandler
	public Resolution view() throws Exception{
		
		String fileUri = "";
		String filename =""; 
		
		try {
			fileUri = this.getContext().getRequest().getParameter("download");
			filename = fileUri.substring(fileUri.lastIndexOf("/")+1);
		} catch (Exception ex){
			addCautionMessage("Not proper use of parameter or not proper filename.");
			return new ForwardResolution("/pages/fileDownloadError.jsp");
		}
		
		
		DownloadFileDTO file = DAOFactory.get().getDao(HelperDAO.class).loadAttachment(fileUri);
		
		if (file.isFileFound()){
			return new StreamingResolution(file.getContentType(), file.getInputStream()).setFilename(filename);
		} else {
			addCautionMessage("Requested file not found.");
			return new ForwardResolution("/pages/fileDownloadError.jsp");
		}
		
		
	}
	
}
