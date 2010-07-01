package eionet.cr.web.action.home;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.UserHistoryDTO;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/history")
public class HistoryActionBean extends AbstractHomeActionBean {

	private List<UserHistoryDTO> history;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_HISTORY);
		return new ForwardResolution("/pages/home/history.jsp");
	}
	
	public List<UserHistoryDTO> getHistory() {
		try {
			history = DAOFactory.get().getDao(HelperDAO.class).getUserHistory(this.getUser());
		} catch (DAOException ex){
			
		}
		return history;
	}
	
	public void setHistory(List<UserHistoryDTO> history) {
		this.history = history;
	}

}
