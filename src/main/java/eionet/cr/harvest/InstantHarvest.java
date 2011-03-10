/*
* The contents of this file are subject to the Mozilla Public
*
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.
*
* The Original Code is Content Registry 2.0.
*
* The Initial Owner of the Original Code is European Environment
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
*
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.harvest;

import java.util.Date;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.persist.PersisterConfig;
import eionet.cr.web.security.CRUser;

public class InstantHarvest extends PullHarvest{

    /** */
    private String userName;

    /**
     *
     * @param sourceUrlString
     * @param lastHarvest
     */
    public InstantHarvest(String sourceUrlString, Date lastHarvest, String userName){

        super(sourceUrlString, lastHarvest);
        this.userName = userName;

        ObjectDTO objectDTO = new ObjectDTO(Subjects.CR_FILE, false);
        objectDTO.setSourceUri(CRUser.registrationsUri(userName));
        sourceMetadata.addObject(Predicates.RDF_TYPE, objectDTO);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.Harvest#createRDFHandler()
     */
    protected RDFHandler createRDFHandler(PersisterConfig config){
        config.setInstantHarvestUser(userName);
        RDFHandler handler = super.createRDFHandler(config);
        return handler;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.Harvest#doHarvestStartedActions()
     */
    protected void doHarvestStartedActions() throws HarvestException{

        logger.debug("Instant harvest started");
        super.doHarvestStartedActions();
    }

    /**
     *
     * @param sourceUrl
     * @param userName
     * @return
     * @throws DAOException
     */
    public static InstantHarvest createFullSetup(String sourceUrl, String userName) throws DAOException{

        HarvestSourceDTO dto = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl);
        InstantHarvest instantHarvest = new InstantHarvest(sourceUrl, null, userName);

        int numOfResources = dto.getResources()==null ? 0 : dto.getResources().intValue();

        instantHarvest.setPreviousHarvest(DAOFactory.get().getDao(
                HarvestDAO.class).getLastHarvestBySourceId(dto.getSourceId().intValue()));
        instantHarvest.setDaoWriter(new HarvestDAOWriter(
                dto.getSourceId().intValue(), Harvest.TYPE_PULL, numOfResources, CRUser.application.getUserName()));
        instantHarvest.setNotificationSender(new HarvestNotificationSender());

        return instantHarvest;
    }
}
