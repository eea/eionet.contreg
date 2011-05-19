package eionet.cr.harvest;

import java.util.Date;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.web.security.CRUser;

public class InstantHarvest extends PullHarvest {

    /**
     *
     * @param sourceUrlString
     * @param lastHarvest
     * @param userName
     */
    public InstantHarvest(String sourceUrlString, Date lastHarvest, String userName) {

        super(sourceUrlString, lastHarvest);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#doHarvestStartedActions()
     */
    protected void doHarvestStartedActions() throws HarvestException {

        logger.debug("Instant virtuoso harvest started");
        super.doHarvestStartedActions();
    }

    /**
     *
     * @param sourceUrl
     * @param userName
     * @return VirtuosoInstantHarvest
     * @throws DAOException
     */
    public static InstantHarvest createFullSetup(String sourceUrl, String userName) throws DAOException {

        HarvestSourceDTO dto = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl);

        InstantHarvest instantHarvest = new InstantHarvest(sourceUrl, null, userName);
        instantHarvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class)
                .getLastHarvestBySourceId(dto.getSourceId().intValue()));
        instantHarvest.setDaoWriter(new HarvestDAOWriter(dto.getSourceId().intValue(), Harvest.TYPE_PULL, CRUser.APPLICATION
                .getUserName()));
        instantHarvest.setNotificationSender(new HarvestNotificationSender());

        return instantHarvest;
    }
}
