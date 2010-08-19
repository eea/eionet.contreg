package eionet.cr.web.action.home;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dao.DAOException;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/uploads")
public class UploadsActionBean extends AbstractHomeActionBean {

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		return new ForwardResolution("/pages/home/uploads.jsp");
	}
	
	
	
	/**
	 * 
	 * @return
	 */
	public Resolution uploadContentFile(){
		
		Resolution resolution = new ForwardResolution("/pages/home/uploadContentFile.jsp");
		if (isPostRequest()){
			
			
			
			resolution = new ForwardResolution("/pages/home/uploads.jsp");
		}
		
		return resolution;
	}
	
	/**
	 * 
	 */
	@Before(stages=LifecycleStage.EventHandling)
	public void setEnvironmentParams(){
		setEnvironmentParams(getContext(), AbstractHomeActionBean.TYPE_UPLOADS);
	}
}
