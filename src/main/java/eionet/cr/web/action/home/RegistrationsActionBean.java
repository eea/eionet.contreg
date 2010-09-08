package eionet.cr.web.action.home;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/registrations")
public class RegistrationsActionBean extends AbstractHomeActionBean {

	private List<TripleDTO> registrations;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		setShowPublic(true);
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_REGISTRATIONS, true);
		return new ForwardResolution("/pages/home/registrations.jsp");
	}
	
	public List<TripleDTO> getRegistrations()  throws DAOException{
		
		if(this.isUserAuthorized() && this.getUser()!=null){
			registrations = DAOFactory.get().getDao(HelperDAO.class).
			getSampleTriplesInSource(this.getUser().getRegistrationsUri(), null);
		}
		else{
			registrations = DAOFactory.get().getDao(HelperDAO.class).
			getSampleTriplesInSource((new CRUser(getAttemptedUserName())).getRegistrationsUri(), null);
		}

		return registrations;
	}
	
	public void setRegistrations(List<TripleDTO> registrations) {
		this.registrations = registrations;
	}
}
