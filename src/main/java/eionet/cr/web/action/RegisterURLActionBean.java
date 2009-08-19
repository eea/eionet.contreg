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
package eionet.cr.web.action;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDao;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.search.SearchException;
import eionet.cr.search.UriSearch;
import eionet.cr.util.Hashes;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/registerUrl.action")
public class RegisterURLActionBean extends AbstractActionBean{
	
	private String url;
	private boolean bookmark = false;

	/**
	 * 
	 * @return
	 * @throws DAOException
	 */
	@DefaultHandler
	public Resolution unspecified(){
		
		return new ForwardResolution("/pages/registerUrl.jsp");
	}
	
	/**
	 * 
	 * @return
	 * @throws SearchException 
	 * @throws DAOException
	 * @throws HarvestException 
	 */
	public Resolution save() throws SearchException, DAOException, HarvestException{
		
		/* get the subject from db */
		
		UriSearch uriSearch = new UriSearch(url);
		uriSearch.execute();
		Collection<SubjectDTO> coll = uriSearch.getResultList();
		SubjectDTO subjectDTO = coll!=null && !coll.isEmpty() ? coll.iterator().next() : null;
		
		// if subject did not exist or it isn't registered in user's registration yet, then add the necessary triples
		if (subjectDTO==null || !subjectDTO.existsPredicateObjectSource(
				Predicates.RDF_TYPE, Subjects.CR_FILE, getCRUser().registrationsUri())){
			
			boolean subjectFirstSeen = subjectDTO==null;
			
			/* add the rdf:type=cr:File triple into user's registrations */
			subjectDTO = new SubjectDTO(url, subjectDTO==null ? false : subjectDTO.isAnonymous());
			ObjectDTO objectDTO = new ObjectDTO(Subjects.CR_FILE, false);
			objectDTO.setSourceUri(getCRUser().registrationsUri());
			subjectDTO.addObject(Predicates.RDF_TYPE, objectDTO);
			
			HelperDao spoHelperDao = factory.getDao(HelperDao.class);			 
			spoHelperDao.addTriples(subjectDTO);
			
			// let the user registrations' URI be stored in RESOURCE
			spoHelperDao.addResource(getCRUser().registrationsUri(), null);
			
			// if this is the first time this subject is seen, store it in RESOURCE
			if (subjectFirstSeen){
				spoHelperDao.addResource(url, getCRUser().registrationsUri());
			}
			
			/* add the URL into user's history, and also into user's bookmarks if requested */
			
			String userHomeUrlHash = CRUser.homeUri(getUserName()) + "/" + Hashes.spoHash(url);			
			subjectDTO = new SubjectDTO(userHomeUrlHash, false);
			objectDTO = new ObjectDTO(Util.dateToString(new Date(), "yyyy-MM-dd'T'HH:mm:ss"), true);
			objectDTO.setSourceUri(getCRUser().historyUri());
			subjectDTO.addObject(Predicates.CR_SAVETIME, objectDTO);
			
			if (bookmark){
				objectDTO = new ObjectDTO(url, false);
				objectDTO.setSourceUri(getCRUser().bookmarksUri());
				subjectDTO.addObject(Predicates.CR_BOOKMARK, objectDTO);
			}
			
			spoHelperDao.addTriples(subjectDTO);
			
			/* add the URL into HARVEST_SOURCE (the dao is responsible for handling if HARVEST_SOURCE already has such a URL) */
			
			HarvestSourceDTO harvestSource = new HarvestSourceDTO();
			harvestSource.setUrl(url);
			harvestSource.setIntervalMinutes(
					Integer.valueOf(
							GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
									String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL))));			
			factory.getDao(HarvestSourceDAO.class).addSourceIgnoreDuplicate(harvestSource, getUserName());
			
			// schedule urgent harvest
			UrgentHarvestQueue.addPullHarvest(url);
		}
		
		// go to factsheet in edit mode
		return new RedirectResolution(FactsheetActionBean.class, "edit").addParameter("uri", url);
	}

	/**
	 * 
	 */
	@ValidationMethod(on="save")
	public void validateSave(){
		
		if (StringUtils.isBlank(url) || !URLUtil.isURL(url)){
			addGlobalError(new SimpleError("Not a valid URL!"));
		}
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @param bookmark the bookmark to set
	 */
	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}
}
