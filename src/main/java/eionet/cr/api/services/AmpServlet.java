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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.api.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import eionet.cr.common.Predicates;
import eionet.cr.dao.ISearchDao;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.PageRequest;
import eionet.cr.util.Pair;

/**
 * Servlet to respond to amp requests.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class AmpServlet extends HttpServlet {

	/**
	 * serial.
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AmpServlet.class);
	private static final String HEADER = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" " +
			"xmlns=\"http://rdfdata.eionet.europa.eu/amp/ontology/\" " +
			"xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n";
	private static final String FOOTER = "</rdf:RDF>";
	private static final Persister persister = new Persister(new Format(4));
	

	/** 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * {@inheritDoc}
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("received request for AMP servlet");
		ISearchDao searchDao = MySQLDAOFactory.get().getDao(ISearchDao.class);
		Map<String, String> criteria = new HashMap<String, String>();
		criteria.put(Predicates.RDF_TYPE, Predicates.AMP_OUTPUT);
		resp.setContentType("text/xml");
		resp.getOutputStream().write(HEADER.getBytes());
		try {
			Pair<Integer, List<SubjectDTO>> results = searchDao.performCustomSearch(
					criteria,
					null, 
					new PageRequest(0, 0),
					null);
			logger.debug("in total " + results.getValue().size() + " records were found");
			if (results.getValue() != null && !results.getValue().isEmpty()) {
				for (SubjectDTO subject : results.getValue()) {
					persister.write(
							new AmpProjectDTO(subject),
							resp.getOutputStream(),
							"UTF-8");
					resp.getOutputStream().write("\n".getBytes());
				}
			}
		} catch (Exception fatal) {
			logger.error("error in AMP servlet", fatal);
			if (!resp.isCommitted()) {
				resp.sendError(500);
			}
		}
		resp.getOutputStream().write(FOOTER.getBytes());
		resp.getOutputStream().flush();
	}

}
