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
	private String exportSource = "";

	@DefaultHandler
	public Resolution download() throws Exception{
		
		if (!uri.isEmpty()) {
			SpoBinaryDTO dto = DAOFactory.get().getDao(SpoBinaryDAO.class).get(uri);
			if (dto==null){
				addCautionMessage("Requested file not found.");
				return new ForwardResolution("/pages/fileDownloadError.jsp");
			}
			
			InputStream contentStream = dto.getContentStream();
			if (contentStream==null || contentStream.available()==0){
				addCautionMessage("Requested file content not found.");
				return new ForwardResolution("/pages/fileDownloadError.jsp");
			}
			
			String filename = StringUtils.substringAfterLast(uri, "/");
			if (StringUtils.isBlank(filename)){
				filename = String.valueOf(Math.abs(Hashes.spoHash(uri)));
			}
			return new StreamingResolution(dto.getContentType(), contentStream).setFilename(filename);
		}
		
		if (!exportSource.isEmpty()){
			return new StreamingResolution("application/rdf+xml"){
		        public void stream(HttpServletResponse response) {
		        	try {
			            OutputStream out = response.getOutputStream();
						RDFExporter.export(Hashes.spoHash(exportSource), out);
		        	} catch (Exception ex){
		        	}
		        }
		    }.setFilename("rdf.xml");
		}
		
		
		return null;
	}
	
	@ValidationMethod(on={"download"})
	public void validate(){
		
		if (StringUtils.isBlank(uri) && StringUtils.isBlank(exportSource)){
			addGlobalValidationError("Uri must not be blank");
			getContext().setSourcePageResolution(new ForwardResolution("/pages/fileDownloadError.jsp"));
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

	public String getExportSource() {
		return exportSource;
	}

	public void setExportSource(String exportSource) {
		this.exportSource = exportSource;
	}
}
