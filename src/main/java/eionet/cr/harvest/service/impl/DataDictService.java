package eionet.cr.harvest.service.impl;

import eionet.cr.dto.ExternalServiceDTO;
import eionet.cr.dto.HarvestScriptDTO;
import eionet.cr.harvest.service.ExternalService;
import eionet.cr.harvest.service.JWTRequestSigner;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Dictionary External Service implementation.
 */
public class DataDictService implements ExternalService {

    /**
     * Default encryption algorithm.
     */
    private final String DD_SERVICE_ENRYPTION_ALGORITHM = "HS512";

    /**
     * Audience param for the external service.
     */
    private final String DD_SERVICE_AUDIENCE = "DataDictionary";

    /**
     * Default timeout for the encryption hash in minutes. 
     */
    private final int DD_SERVICE_TIMEOUT_MIN = 71;
    
    
    
    /**
     * Helper for signing push requests.
     */
    private static JWTRequestSigner pushRequestAuthenticator = new JWTRequestSigner();

    @Override
    public PostMethod buildPost(ExternalServiceDTO serviceDTO, HarvestScriptDTO scriptDto, File file) throws Exception {

        String url = serviceDTO.getServiceUrl();
        if (StringUtils.isNotBlank(scriptDto.getExternalServiceParams())) {
            //TODO - utf-8?
            url += scriptDto.getExternalServiceParams();
        }
        
        PostMethod post = new PostMethod(url);
        post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(file), file.length()));
        post.setRequestHeader("Content-type", "application/rdf+xml; charset=utf-8");

        Map<String, String> jwtPayload = new HashMap<String, String>();
        jwtPayload.put("API_KEY", serviceDTO.getUserId());

        post.setRequestHeader("X-DD-API-KEY", pushRequestAuthenticator.sign(serviceDTO.getSecureToken(), DD_SERVICE_AUDIENCE,
                jwtPayload, DD_SERVICE_TIMEOUT_MIN, DD_SERVICE_ENRYPTION_ALGORITHM));

        return post;
    }
}
