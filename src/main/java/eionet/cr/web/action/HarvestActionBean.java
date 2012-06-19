/*
 * The contents of this file are subject to the Mozilla Public
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
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.util.HarvestMessageType;

/**
 *
 * @author heinljab
 *
 */
@UrlBinding("/harvest.action")
public class HarvestActionBean extends AbstractActionBean {

    /** */
    private HarvestDTO harvestDTO;
    private HarvestSourceDTO harvestSourceDTO;

    /** */
    private List<HarvestMessageDTO> fatals;
    private List<HarvestMessageDTO> errors;
    private List<HarvestMessageDTO> warnings;
    private List<HarvestMessageDTO> infos;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DontValidate
    public Resolution unspecified() throws DAOException {
        DAOFactory factory = DAOFactory.get();
        harvestDTO = factory.getDao(HarvestDAO.class).getHarvestById(harvestDTO.getHarvestId());
        harvestSourceDTO = factory.getDao(HarvestSourceDAO.class).getHarvestSourceById(harvestDTO.getHarvestSourceId());
        loadMessages();
        return new ForwardResolution("/pages/harvest.jsp");
    }

    /**
     * @throws DAOException
     *
     */
    private void loadMessages() throws DAOException {

        List<HarvestMessageDTO> messageDTOs =
            DAOFactory.get().getDao(HarvestMessageDAO.class).findHarvestMessagesByHarvestID(harvestDTO.getHarvestId());
        if (messageDTOs != null) {

            fatals = new ArrayList<HarvestMessageDTO>();
            errors = new ArrayList<HarvestMessageDTO>();
            warnings = new ArrayList<HarvestMessageDTO>();
            infos = new ArrayList<HarvestMessageDTO>();

            loadMessages(HarvestMessageType.FATAL.toString(), messageDTOs, fatals);
            loadMessages(HarvestMessageType.ERROR.toString(), messageDTOs, errors);
            loadMessages(HarvestMessageType.WARNING.toString(), messageDTOs, warnings);
            loadMessages(HarvestMessageType.INFO.toString(), messageDTOs, infos);
        }
    }

    /**
     *
     * @param messageDTOs
     * @param type
     */
    private void loadMessages(String type, List<HarvestMessageDTO> sourceList, List<HarvestMessageDTO> destList) {

        for (int i = 0; i < sourceList.size(); i++) {
            HarvestMessageDTO messageDTO = sourceList.get(i);
            if (messageDTO.getType().equals(type)) {
                destList.add(messageDTO);
            }
        }
    }

    /**
     * @return the harvestDTO
     */
    public HarvestDTO getHarvestDTO() {
        return harvestDTO;
    }

    /**
     * @param harvestDTO the harvestDTO to set
     */
    public void setHarvestDTO(HarvestDTO harvestDTO) {
        this.harvestDTO = harvestDTO;
    }

    /**
     * @return the harvestSourceDTO
     */
    public HarvestSourceDTO getHarvestSourceDTO() {
        return harvestSourceDTO;
    }

    /**
     * @return the fatals
     */
    public List<HarvestMessageDTO> getFatals() {
        return fatals;
    }

    /**
     * @return the errors
     */
    public List<HarvestMessageDTO> getErrors() {
        return errors;
    }

    /**
     * @return the warnings
     */
    public List<HarvestMessageDTO> getWarnings() {
        return warnings;
    }

    /**
     * @return the infos
     */
    public List<HarvestMessageDTO> getInfos() {
        return infos;
    }
}
