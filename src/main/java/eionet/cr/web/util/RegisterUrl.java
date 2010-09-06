package eionet.cr.web.util;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.web.security.CRUser;

public class RegisterUrl {

	public static void register(String url, CRUser user, boolean saveToBookmarks) throws DAOException, HarvestException{
		// register URL
		DAOFactory.get().getDao(HelperDAO.class).registerUserUrl(user, url, saveToBookmarks);
		
		String _url = StringUtils.substringBefore(url, "#");
		Integer intervalMinutes = Integer.valueOf(
				GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
						String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL)));

		// add the URL into HARVEST_SOURCE
		// (the dao is responsible for handling if HARVEST_SOURCE already has such a URL)
		DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIgnoreDuplicate(_url, intervalMinutes, true, null);
		
		// schedule urgent harvest
		UrgentHarvestQueue.addPullHarvest(_url);
	}
	
}
