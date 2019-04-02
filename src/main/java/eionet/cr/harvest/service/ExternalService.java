package eionet.cr.harvest.service;

import eionet.cr.dto.ExternalServiceDTO;
import eionet.cr.dto.HarvestScriptDTO;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.File;

/**
 * Created by kaido on 6.01.2016.
 */
public interface ExternalService {

    /**
     * Builds a post request specific for this kind of service.
     * @param serviceDTO DTO including special service related data
     * @param scriptDTO DTO with the harvest script data
     * @param file file the rdf content is loaded from
     * @return generated post method object
     * @throws java.lang.Exception if generating post files
     */
    PostMethod buildPost(ExternalServiceDTO serviceDTO, HarvestScriptDTO scriptDTO, File file) throws Exception;
}
