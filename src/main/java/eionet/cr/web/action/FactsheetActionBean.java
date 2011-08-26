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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.OnDemandHarvester;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.SubjectDTOOptimizer;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;

/**
 * Factsheet.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/factsheet.action")
public class FactsheetActionBean extends AbstractActionBean {

    /** */
    private static final String ADDIBLE_PROPERTIES_SESSION_ATTR = FactsheetActionBean.class.getName() + ".addibleProperties";

    /** */
    private String uri;
    /** Hashed URI. */
    private long uriHash;
    /** subject dataobject. */
    private SubjectDTO subject;

    /** */
    private Map<String, String> predicateLabels;
    /** Resource sub-properties. */
    private SubProperties subProperties;

    /** */
    private boolean anonymous;
    /**
     * property URI.
     */
    private String propertyUri;
    /** */
    private String propertyValue;

    /** */
    private List<String> rowId;

    /** */
    private boolean noCriteria;
    /** */
    private boolean adminLoggedIn;

    /** */
    private Boolean subjectIsUserBookmark;

    /** */
    private Boolean uriIsHarvestSource;

    /** */
    private boolean subjectDownloadable;

    /**
     *
     * @return Resolution
     * @throws DAOException
     *             if query fails
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isBlank(uri) && uriHash == 0) {
            noCriteria = true;
            addCautionMessage("Resource identifier not specified!");
        } else {
            Long subjectHash = uriHash == 0 ? Hashes.spoHash(uri) : uriHash;
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);

            setAdminLoggedIn(getUser() != null && getUser().isAdministrator());

            subject = helperDAO.getSubject(uri);
            if (subject != null) {

                if (getContext().getRequest().getParameter("nofilter") == null) {
                    subject = SubjectDTOOptimizer.optimizeSubjectDTOFactsheetView(subject, getAcceptedLanguagesByImportance());
                }

                uri = subject.getUri();
                uriHash = subject.getUriHash();

                PredicateLabels predLabels = helperDAO.getPredicateLabels(Collections.singleton(subjectHash));
                if (predLabels != null) {
                    predicateLabels = predLabels.getByLanguages(getAcceptedLanguages());
                }
                subProperties = helperDAO.getSubProperties(subject.getPredicates().keySet());

                logger.debug("Determining if the subject has content stored in database");

                subjectDownloadable = DAOFactory.get().getDao(SpoBinaryDAO.class).exists(uri);
            }
        }

        return new ForwardResolution("/pages/factsheet.jsp");
    }

    /**
     * Handle for ajax harvesting.
     *
     * @return Resolution
     */
    public Resolution harvestAjax() {
        String message;
        try {
            message = harvestNow().getRight();
        } catch (Exception ignored) {
            logger.error("error while scheduling ajax harvest", ignored);
            message = "Error occured, more info can be obtained in application logs";
        }
        return new StreamingResolution("text/html", message);
    }

    /**
     * Schedules a harvest for resource.
     *
     * @return view resolution
     * @throws HarvestException
     *             if harvesting fails
     * @throws DAOException
     *             if query fails
     */
    public Resolution harvest() throws HarvestException, DAOException {

        Pair<Boolean, String> message = harvestNow();
        if (message.getLeft()) {
            addWarningMessage(message.getRight());
        } else {
            addSystemMessage(message.getRight());
        }

        return new RedirectResolution(this.getClass(), "view").addParameter("uri", uri);
    }

    /**
     * helper method to eliminate code duplication.
     *
     * @return Pair<Boolean, String> feedback messages
     * @throws HarvestException
     *             if harvesting fails
     * @throws DAOException
     *             if query fails
     */
    private Pair<Boolean, String> harvestNow() throws HarvestException, DAOException {

        String message = null;
        if (isUserLoggedIn()) {
            if (!StringUtils.isBlank(uri) && URLUtil.isURL(uri)) {

                /* add this url into HARVEST_SOURCE table */

                HarvestSourceDAO dao = factory.getDao(HarvestSourceDAO.class);
                HarvestSourceDTO dto = new HarvestSourceDTO();
                dto.setUrl(StringUtils.substringBefore(uri, "#"));
                dto.setEmails("");
                dto.setIntervalMinutes(Integer.valueOf(GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
                        String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL))));
                dto.setPrioritySource(false);
                dto.setOwner(null);
                dao.addSourceIgnoreDuplicate(dto);

                /* issue an instant harvest of this url */

                OnDemandHarvester.Resolution resolution = OnDemandHarvester.harvest(dto.getUrl(), getUserName());

                /* give feedback to the user */

                if (resolution.equals(OnDemandHarvester.Resolution.ALREADY_HARVESTING))
                    message = "The resource is currently being harvested by another user or background harvester!";
                else if (resolution.equals(OnDemandHarvester.Resolution.UNCOMPLETE))
                    message = "The harvest hasn't finished yet, but continues in the background!";
                else if (resolution.equals(OnDemandHarvester.Resolution.COMPLETE))
                    message = "The harvest has been completed!";
                else if (resolution.equals(OnDemandHarvester.Resolution.SOURCE_UNAVAILABLE))
                    message = "The resource was not available!";
                else if (resolution.equals(OnDemandHarvester.Resolution.NO_STRUCTURED_DATA))
                    message = "The resource contained no RDF data!";
                //                else if (resolution.equals(InstantHarvester.Resolution.RECENTLY_HARVESTED))
                //                    message = "Source redirects to another source that has recently been harvested! Will not harvest.";
                else
                    message = "No feedback given from harvest!";
            }
            return new Pair<Boolean, String>(false, message);
        } else {
            return new Pair<Boolean, String>(true, getBundle().getString("not.logged.in"));
        }
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     *             if query fails if query fails
     */
    public Resolution edit() throws DAOException {

        return view();
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     *             if query fails if query fails
     */
    public Resolution addbookmark() throws DAOException {
        if (isUserLoggedIn()) {
            DAOFactory.get().getDao(HelperDAO.class).addUserBookmark(getUser(), getUrl());
            addSystemMessage("Succesfully bookmarked this source.");
        } else {
            addSystemMessage("Only logged in users can bookmark sources.");
        }
        return view();
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     *             if query fails
     */
    public Resolution removebookmark() throws DAOException {
        if (isUserLoggedIn()) {
            DAOFactory.get().getDao(HelperDAO.class).deleteUserBookmark(getUser(), getUrl());
            addSystemMessage("Succesfully removed this source from bookmarks.");
        } else {
            addSystemMessage("Only logged in users can remove bookmarks.");
        }
        return view();
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     *             if query fails if query fails
     */
    public Resolution save() throws DAOException {

        SubjectDTO subjectDTO = new SubjectDTO(uri, anonymous);

        if (propertyUri.equals(Predicates.CR_TAG)) {
            List<String> tags = Util.splitStringBySpacesExpectBetweenQuotes(propertyValue);

            for (String tag : tags) {
                ObjectDTO objectDTO = new ObjectDTO(tag, true);
                objectDTO.setSourceUri(getUser().getRegistrationsUri());
                subjectDTO.addObject(propertyUri, objectDTO);
            }
        } else {
            // other properties
            ObjectDTO objectDTO = new ObjectDTO(propertyValue, true);
            objectDTO.setSourceUri(getUser().getRegistrationsUri());
            subjectDTO.addObject(propertyUri, objectDTO);
        }

        HelperDAO helperDao = factory.getDao(HelperDAO.class);
        helperDao.addTriples(subjectDTO);
        helperDao.updateUserHistory(getUser(), uri);

        // since user registrations URI was used as triple source, add it to HARVEST_SOURCE too
        // (but set interval minutes to 0, to avoid it being background-harvested)
        DAOFactory
        .get()
        .getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(
                HarvestSourceDTO.create(getUser().getRegistrationsUri(), true, 0, getUser().getUserName()));

        return new RedirectResolution(this.getClass(), "edit").addParameter("uri", uri);
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     *             if query fails
     */
    public Resolution delete() throws DAOException {

        if (rowId != null && !rowId.isEmpty()) {

            ArrayList<TripleDTO> triples = new ArrayList<TripleDTO>();

            for (String row : rowId) {
                int i = row.indexOf("_");
                if (i <= 0 || i == (row.length() - 1)) {
                    throw new IllegalArgumentException("Illegal rowId: " + row);
                }

                String predicateHash = row.substring(0, i);
                String predicate = getContext().getRequestParameter("pred_".concat(predicateHash));

                String objectHash = row.substring(i + 1);
                String objectValue = getContext().getRequest().getParameter("obj_".concat(objectHash));
                String sourceUri = getContext().getRequest().getParameter("source_".concat(objectHash));

                TripleDTO triple = new TripleDTO(uri, predicate, objectValue);
                // FIXME - find a better way to determine if the object is literal or not, URIs may be literals also
                triple.setLiteralObject(!URLUtil.isURL(objectValue));
                triple.setSourceUri(sourceUri);

                triples.add(triple);
            }

            HelperDAO helperDao = factory.getDao(HelperDAO.class);
            helperDao.deleteTriples(triples);
            helperDao.updateUserHistory(getUser(), uri);
        }

        return new RedirectResolution(this.getClass(), "edit").addParameter("uri", uri);
    }

    /**
     * Validates if user is logged on and if event property is not empty.
     */
    @ValidationMethod(on = { "save", "delete", "edit", "harvest" })
    public void validateUserKnown() {

        if (getUser() == null) {
            addWarningMessage("Operation not allowed for anonymous users");
        } else if (getContext().getEventName().equals("save") && StringUtils.isBlank(propertyValue)) {
            addGlobalValidationError(new SimpleError("Property value must not be blank"));
        }
    }

    /**
     * @return the resourceUri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param resourceUri
     *            the resourceUri to set
     */
    public void setUri(final String resourceUri) {
        this.uri = resourceUri;
    }

    /**
     * @return the resource
     */
    public SubjectDTO getSubject() {
        return subject;
    }

    /**
     * @return the predicateLabels
     */
    public Map<String, String> getPredicateLabels() {
        return predicateLabels;
    }

    /**
     * @return the subProperties
     */
    public SubProperties getSubProperties() {
        return subProperties;
    }

    /**
     * @return the addibleProperties
     * @throws DAOException
     *             if query fails
     */
    public Collection<UriLabelPair> getAddibleProperties() throws DAOException {

        /* get the addible properties from session */

        HttpSession session = getContext().getRequest().getSession();
        @SuppressWarnings("unchecked")
        ArrayList<UriLabelPair> result = (ArrayList<UriLabelPair>) session.getAttribute(ADDIBLE_PROPERTIES_SESSION_ATTR);

        // if not in session, create them and add to session
        if (result == null || result.isEmpty()) {

            /* get addible properties from database */

            HelperDAO helperDAO = factory.getDao(HelperDAO.class);
            HashMap<String, String> props = helperDAO.getAddibleProperties(getSubjectTypes());

            // add some hard-coded properties, HashMap assures there won't be
            // duplicates
            // props.put(Predicates.RDF_TYPE, "Type");
            props.put(Predicates.RDFS_LABEL, "Title");
            props.put(Predicates.CR_TAG, "Tag");
            props.put(Predicates.RDFS_COMMENT, "Other comments"); // Don't use
            // CR_COMMENT
            props.put(Predicates.DC_DESCRIPTION, "Description");
            props.put(Predicates.CR_HAS_SOURCE, "hasSource");
            props.put(Predicates.ROD_PRODUCT_OF, "productOf");

            /*
             * create the result object from the found and hard-coded properties, sort it
             */

            result = new ArrayList<UriLabelPair>();
            if (props != null && !props.isEmpty()) {

                for (String uri : props.keySet()) {
                    result.add(UriLabelPair.create(uri, props.get(uri)));
                }
                Collections.sort(result);
            }

            // put into session
            session.setAttribute(ADDIBLE_PROPERTIES_SESSION_ATTR, result);
        }

        return result;
    }

    /**
     * List of subject hashes.
     *
     * @return Collection <String> hashes of subject types
     */
    private Collection<String> getSubjectTypes() {

        HashSet<String> result = new HashSet<String>();
        Collection<ObjectDTO> typeObjects = subject.getObjects(Predicates.RDF_TYPE, ObjectDTO.Type.RESOURCE);
        if (typeObjects != null && !typeObjects.isEmpty()) {

            for (ObjectDTO object : typeObjects) {

                result.add(object.getValue());
            }
        }

        return result;
    }

    /**
     * @param anonymous
     *            the anonymous to set
     */
    public void setAnonymous(final boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * @param subject
     *            the subject to set
     */
    public void setSubject(final SubjectDTO subject) {
        this.subject = subject;
    }

    /**
     * @param predicateLabels
     *            the predicateLabels to set
     */
    public void setPredicateLabels(final Map<String, String> predicateLabels) {
        this.predicateLabels = predicateLabels;
    }

    /**
     * @param subProperties
     *            the subProperties to set
     */
    public void setSubProperties(final SubProperties subProperties) {
        this.subProperties = subProperties;
    }

    /**
     * @param propertyUri
     *            the propertyUri to set
     */
    public void setPropertyUri(final String propertyUri) {
        this.propertyUri = propertyUri;
    }

    /**
     * @param propertyValue
     *            the propertyValue to set
     */
    public void setPropertyValue(final String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * @param rowId
     *            the rowId to set
     */
    public void setRowId(final List<String> rowId) {
        this.rowId = rowId;
    }

    /**
     * @return the noCriteria
     */
    public boolean isNoCriteria() {
        return noCriteria;
    }

    /**
     * @return the uriHash
     */
    public long getUriHash() {
        return uriHash;
    }

    /**
     * @param uriHash
     *            the uriHash to set
     */
    public void setUriHash(final long uriHash) {
        this.uriHash = uriHash;
    }

    /**
     *
     * @return String
     */
    public String getUrl() {
        return uri != null && URLUtil.isURL(uri) ? uri : null;
    }

    /**
     * True if admin is logged in.
     *
     * @return boolean
     */
    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    /**
     * Setter of admin logged in property.
     *
     * @param adminLoggedIn
     *            boolean
     */
    public void setAdminLoggedIn(final boolean adminLoggedIn) {
        this.adminLoggedIn = adminLoggedIn;
    }

    /**
     *
     * @return boolean
     * @throws DAOException
     *             if query fails if query fails
     */
    public boolean getSubjectIsUserBookmark() throws DAOException {

        if (!isUserLoggedIn()) {
            return false;
        }

        if (subjectIsUserBookmark == null) {
            subjectIsUserBookmark = Boolean.valueOf(factory.getDao(HelperDAO.class).isSubjectUserBookmark(getUser(), uri));
        }

        return subjectIsUserBookmark.booleanValue();
    }

    /**
     * @return the uriIsHarvestSource
     * @throws DAOException
     *             if query fails
     */
    public Boolean getUriIsHarvestSource() throws DAOException {

        if (uriIsHarvestSource == null) {

            if ((uri == null && subject == null) || (subject != null && subject.isAnonymous())) {
                uriIsHarvestSource = Boolean.FALSE;
            } else {
                String s = subject != null ? subject.getUri() : uri;
                HarvestSourceDTO dto = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(s);
                uriIsHarvestSource = dto == null ? Boolean.FALSE : Boolean.TRUE;
            }
        }
        return uriIsHarvestSource;
    }

    /**
     * @return the subjectDownloadable
     */
    public boolean isSubjectDownloadable() {
        return subjectDownloadable;
    }

    /**
     *
     * @return boolean
     */
    public boolean isCurrentlyHarvested() {

        return uri == null ? false : CurrentHarvests.contains(uri);
    }

    /**
     * True if the resource can be shown on a map. The resource has got longitude and latitude predicates
     *
     * @return boolean
     */
    public boolean isMapDisplayable() {
        // TODO subproperties handling
        if (subject != null) {
            Collection<ObjectDTO> objects = subject.getObjects(Predicates.RDF_TYPE, ObjectDTO.Type.RESOURCE);
            if (objects != null) {
                boolean isWgsPoint = false;
                for (ObjectDTO objectDTO : objects) {
                    if (objectDTO.getValue() != null && objectDTO.getValue().equals(Subjects.WGS_POINT)) {
                        isWgsPoint = true;
                        break;
                    }
                }

                if (isWgsPoint) {
                    logger.debug("FactSheetActionBean() isWgsPoint=true");
                    if (subject.getObject(Predicates.WGS_LAT) != null && subject.getObject(Predicates.WGS_LONG) != null) {
                        logger.debug("FactSheetActionBean() has coordinates");
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     *
     * @return Resolution
     */
    public Resolution showOnMap() {
        return new ForwardResolution("/pages/map.jsp");
    }

    /**
     * Longitude of the resource if the resouce is map displayable.
     *
     * returns null if not set
     *
     * @return String longitude
     */
    public String getLongitude() {
        // TODO subproperties handling
        if (subject.getObject(Predicates.WGS_LONG) != null) {
            return subject.getObject(Predicates.WGS_LONG).getValue();
        }
        return null;
    }

    /**
     * Latitude of the resource if the resouce is map displayable. returns null if not set
     *
     * @return String latitude
     */
    public String getLatitude() {
        // TODO subproperties handling
        if (subject.getObject(Predicates.WGS_LAT) != null) {
            return subject.getObject(Predicates.WGS_LAT).getValue();
        }
        return null;
    }

}
