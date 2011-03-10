package eionet.cr.web.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dto.SpoBinaryDTO;
import eionet.cr.util.Hashes;

/**
 *
 * @author <a href="mailto:jaak.kapten@tietoenator.com">Jaak Kapten</a>
 *
 */
@UrlBinding("/download.action")

public class DownloadActionBean extends AbstractActionBean {

    /** */
    private String uri = "";

    /**
     *
     * @return
     * @throws Exception
     */
    @DefaultHandler
    public Resolution download() throws Exception{

        // get the binary content DTO of the requested file
        SpoBinaryDTO dto = DAOFactory.get().getDao(SpoBinaryDAO.class).get(uri);
        if (dto==null){
            addCautionMessage("Requested file not found.");
            return new ForwardResolution("/pages/empty.jsp");
        }

        // get the actual content stream
        InputStream contentStream = dto.getContentStream();
        if (contentStream==null || contentStream.available()==0){
            addCautionMessage("Requested file content not found.");
            return new ForwardResolution("/pages/empty.jsp");
        }

        // prepare name for the file to be streamed to the user
        String filename = StringUtils.substringAfterLast(uri, "/");
        if (StringUtils.isBlank(filename)){
            filename = String.valueOf(Math.abs(Hashes.spoHash(uri)));
        }

        // stream the above-received content to the user
        return new StreamingResolution(dto.getContentType(), contentStream).setFilename(filename);
    }

    @ValidationMethod(on={"download"})
    public void validate(){

        if (StringUtils.isBlank(uri)){
            addGlobalValidationError("Uri must not be blank");
            getContext().setSourcePageResolution(new ForwardResolution("/pages/empty.jsp"));
        }
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
