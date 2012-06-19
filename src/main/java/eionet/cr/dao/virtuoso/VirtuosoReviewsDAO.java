package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.ReviewsDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 * Virtuoso DAO for reviews functionality.
 */
public class VirtuosoReviewsDAO extends VirtuosoBaseDAO implements ReviewsDAO {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#generateNewReviewId(eionet.cr.web.security.CRUser )
     */
    @Override
    public int generateNewReviewId(CRUser user) throws DAOException {

        int currentLastId = getLastReviewId(user);
        int newId = 0;

        // Deleting from the database the old value and creating a new one.
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            URI context = conn.getValueFactory().createURI(user.getHomeUri());
            URI sub = conn.getValueFactory().createURI(user.getHomeUri());
            URI pred = conn.getValueFactory().createURI(Predicates.CR_USER_REVIEW_LAST_NUMBER);

            conn.remove(sub, pred, null, context);

            // Generating new ID
            newId = currentLastId + 1;
            Literal newVal = conn.getValueFactory().createLiteral(String.valueOf(newId), XMLSchema.INTEGER);
            conn.add(sub, pred, newVal, context);

        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(conn);
        }

        // since user's home URI was used above as triple source, add it to HARVEST_SOURCE too
        // (but set interval minutes to 0, to avoid it being background-harvested)
        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(user.getHomeUri(), true, 0, user.getUserName()));

        return newId;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getLastReviewId(eionet.cr.web.security.CRUser)
     */
    @Override
    public int getLastReviewId(CRUser user) throws DAOException {

        int lastid = 0;

        String subjectUrl = user.getHomeUri();
        String sparql = "select ?o where {?s ?p ?o}";
        Bindings bindings = new Bindings();
        bindings.setURI("s", subjectUrl);
        bindings.setURI("p", Predicates.CR_USER_REVIEW_LAST_NUMBER);
        String id = executeUniqueResultSPARQL(sparql, bindings, new SingleObjectReader<String>());
        if (!StringUtils.isBlank(id)) {
            lastid = Integer.parseInt(id);
        }

        return lastid;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#addReview(eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
     */
    @Override
    public int addReview(ReviewDTO review, CRUser user) throws DAOException {
        int reviewId = generateNewReviewId(user);
        insertReviewToDB(review, user, reviewId);
        return reviewId;
    }

    private void insertReviewToDB(ReviewDTO review, CRUser user, int reviewId) throws DAOException {

        String objectUrl = StringUtils.replace(review.getObjectUrl(), " ", "%20");

        String userReviewUri = user.getReviewUri(reviewId);
        SubjectDTO newReview = new SubjectDTO(userReviewUri, false);

        ObjectDTO typeObject = new ObjectDTO(Subjects.CR_FEEDBACK, false);
        typeObject.setSourceUri(userReviewUri);

        ObjectDTO titleObject = new ObjectDTO(review.getTitle(), true);
        titleObject.setSourceUri(userReviewUri);

        ObjectDTO feedbackForObject = new ObjectDTO(objectUrl, false);
        feedbackForObject.setSourceUri(userReviewUri);

        ObjectDTO feedbackUserObject = new ObjectDTO(user.getHomeUri(), false);
        feedbackUserObject.setSourceUri(userReviewUri);

        ObjectDTO contentLastModifiedObject = new ObjectDTO(Util.virtuosoDateToString(new Date()), true, XMLSchema.DATETIME);
        contentLastModifiedObject.setSourceUri(userReviewUri);

        newReview.addObject(Predicates.RDF_TYPE, typeObject);
        newReview.addObject(Predicates.DC_TITLE, titleObject);
        newReview.addObject(Predicates.RDFS_LABEL, titleObject);
        newReview.addObject(Predicates.CR_FEEDBACK_FOR, feedbackForObject);
        newReview.addObject(Predicates.CR_USER, feedbackUserObject);
        newReview.addObject(Predicates.CR_LAST_MODIFIED, contentLastModifiedObject);

        DAOFactory.get().getDao(HelperDAO.class).addTriples(newReview);

        // creating a cross link to show that specific object has a review.
        SubjectDTO crossLinkSubject = new SubjectDTO(objectUrl, false);
        ObjectDTO grossLinkObject = new ObjectDTO(userReviewUri, false);
        grossLinkObject.setSourceUri(userReviewUri);
        crossLinkSubject.addObject(Predicates.CR_HAS_FEEDBACK, grossLinkObject);
        DAOFactory.get().getDao(HelperDAO.class).addTriples(crossLinkSubject);

        // since the review URI was used above as triple source, add it to HARVEST_SOURCE too
        // (but set interval minutes to 0, to avoid it being background-harvested)
        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(userReviewUri, true, 0, user.getUserName()));
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#saveReview(int, eionet.cr.dto.ReviewDTO, eionet.cr.web.security.CRUser)
     */
    @Override
    public void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException {
        deleteReview(user, reviewId, false);
        insertReviewToDB(review, user, reviewId);

    }

    /** */
    private static final String USER_REVIEWS_SPARQL = "select ?s ?p ?o where { ?s ?p ?o. { select distinct ?s where { "
        + "?s ?type ?feedback. ?s ?user ?userHomeUri }}} order by ?s ?p ?o";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReviewList(eionet.cr.web.security.CRUser)
     */
    @Override
    public List<ReviewDTO> getReviewList(CRUser user) throws DAOException {

        Bindings bindings = new Bindings();
        bindings.setURI("userHomeUri", user.getHomeUri());
        bindings.setURI("type", Predicates.RDF_TYPE);
        bindings.setURI("feedback", Subjects.CR_FEEDBACK);
        bindings.setURI("user", Predicates.CR_USER);

        RepositoryConnection conn = null;
        List<ReviewDTO> resultList = new ArrayList<ReviewDTO>();
        TupleQueryResult queryResult = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, USER_REVIEWS_SPARQL);
            bindings.applyTo(tupleQuery, conn.getValueFactory());
            queryResult = tupleQuery.evaluate();

            ReviewDTO reviewDTO = null;
            while (queryResult.hasNext()) {

                BindingSet bindingSet = queryResult.next();

                String reviewUri = bindingSet.getValue("s").stringValue();
                if (reviewDTO == null || !reviewUri.equals(reviewDTO.getReviewSubjectUri())) {
                    reviewDTO = new ReviewDTO();
                    reviewDTO.setReviewSubjectUri(reviewUri);
                    resultList.add(reviewDTO);
                }

                String predicateUri = bindingSet.getValue("p").stringValue();
                if (predicateUri.equals(Predicates.DC_TITLE)) {
                    reviewDTO.setTitle(bindingSet.getValue("o").stringValue());
                }

                if (predicateUri.equals(Predicates.CR_FEEDBACK_FOR)) {
                    reviewDTO.setObjectUrl(bindingSet.getValue("o").stringValue());
                }
            }
        } catch (OpenRDFException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(queryResult);
            SesameUtil.close(conn);
        }

        return resultList;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReview(eionet.cr.web.security.CRUser, int)
     */
    @Override
    public ReviewDTO getReview(CRUser user, int reviewId) throws DAOException {

        String reviewUri = user.getReviewUri(reviewId);
        ReviewDTO reviewDTO = getReviewDTO(reviewUri);
        return reviewDTO;
    }

    private static final String REVIEW_SPARQL = "select ?p ?o where { ?reviewUri ?p ?o }";

    /**
     * Get title and feedbackFor reference for a review.
     *
     * @param reviewUri - URI of review.
     * @return ReviewDTO
     * @throws DAOException if query fails
     */
    private ReviewDTO getReviewDTO(String reviewUri) throws DAOException {
        Bindings bindings = new Bindings();
        bindings.setURI("reviewUri", reviewUri);

        ReviewDTO reviewDTO = null;
        RepositoryConnection conn = null;
        TupleQueryResult queryResult = null;

        try {
            conn = SesameUtil.getRepositoryConnection();
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, REVIEW_SPARQL);
            bindings.applyTo(tupleQuery, conn.getValueFactory());
            queryResult = tupleQuery.evaluate();

            while (queryResult.hasNext()) {

                BindingSet bindingSet = queryResult.next();

                if (reviewDTO == null) {
                    reviewDTO = new ReviewDTO();
                    reviewDTO.setReviewSubjectUri(reviewUri);
                }

                String predicateUri = bindingSet.getValue("p").stringValue();
                if (predicateUri.equals(Predicates.DC_TITLE)) {
                    reviewDTO.setTitle(bindingSet.getValue("o").stringValue());
                }
                if (predicateUri.equals(Predicates.CR_FEEDBACK_FOR)) {
                    reviewDTO.setObjectUrl(bindingSet.getValue("o").stringValue());
                }
            }
        } catch (OpenRDFException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(queryResult);
            SesameUtil.close(conn);
        }

        return reviewDTO;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.ReviewsDAO#isReviewObsolete(java.util.String reviewUri, java.util.String objectUri)
     */
    @Override
    public boolean isReviewObsolete(String reviewUri, String objectUri) throws DAOException {

        boolean ret = true;
        String query =
            "ASK where {?objectUri ?pred ?date1 . ?reviewUri ?pred ?date2 . FILTER (?date2 >= ?date1) }";

        RepositoryConnection con = null;
        try {
            con = SesameConnectionProvider.getReadOnlyRepositoryConnection();

            Bindings bindings = new Bindings();
            bindings.setURI("objectUri", objectUri);
            bindings.setURI("reviewUri", reviewUri);
            bindings.setURI("pred", Predicates.CR_LAST_MODIFIED);

            BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            bindings.applyTo(booleanQuery, con.getValueFactory());
            Boolean result = booleanQuery.evaluate();
            if (result != null) {
                if (result.booleanValue()) {
                    ret = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(con);
        }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#getReviewAttachmentList(eionet.cr.web.security .CRUser, int)
     */
    @Override
    public List<String> getReviewAttachmentList(CRUser user, int reviewId) throws DAOException {

        String sparql = "select ?o where {?s ?p ?o}";
        Bindings bindings = new Bindings();
        bindings.setURI("s", user.getReviewUri(reviewId));
        bindings.setURI("p", Predicates.CR_HAS_ATTACHMENT);
        List<String> returnList = executeSPARQL(sparql, bindings, new SingleObjectReader<String>());
        return returnList;

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteReview(eionet.cr.web.security.CRUser, int, boolean)
     */
    @Override
    public void deleteReview(CRUser user, int reviewId, boolean deleteAttachments) throws DAOException {

        String reviewSubjectURI = user.getReviewUri(reviewId);

        // Deleting from the database the old value and creating a new one.
        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();

            URI context = conn.getValueFactory().createURI(reviewSubjectURI);
            URI sub = conn.getValueFactory().createURI(reviewSubjectURI);

            URI pred = conn.getValueFactory().createURI(Predicates.RDF_TYPE);
            conn.remove(sub, pred, null, context);

            pred = conn.getValueFactory().createURI(Predicates.DC_TITLE);
            conn.remove(sub, pred, null, context);

            pred = conn.getValueFactory().createURI(Predicates.RDFS_LABEL);
            conn.remove(sub, pred, null, context);

            pred = conn.getValueFactory().createURI(Predicates.CR_FEEDBACK_FOR);
            conn.remove(sub, pred, null, context);

            pred = conn.getValueFactory().createURI(Predicates.CR_USER);
            conn.remove(sub, pred, null, context);

            pred = conn.getValueFactory().createURI(Predicates.CR_LAST_MODIFIED);
            conn.remove(sub, pred, null, context);

            // Remove cross-link
            String sparql = "select ?s where {?s <" + Predicates.CR_HAS_FEEDBACK + "> <" + reviewSubjectURI + ">}";
            String reviewObject = executeUniqueResultSPARQL(sparql, new SingleObjectReader<String>());
            if (!StringUtils.isBlank(reviewObject)) {
                URI reviewObjectSub = conn.getValueFactory().createURI(reviewObject);
                pred = conn.getValueFactory().createURI(Predicates.CR_HAS_FEEDBACK);
                conn.remove(reviewObjectSub, pred, sub, context);
            }

            if (deleteAttachments) {
                pred = conn.getValueFactory().createURI(Predicates.CR_HAS_ATTACHMENT);
                conn.remove(sub, pred, null, context);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException(e.getMessage(), e);
        } finally {
            SesameUtil.close(conn);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HelperDAO#deleteAttachment(eionet.cr.web.security.CRUser, int, java.lang.String)
     */
    @Override
    public void deleteAttachment(CRUser user, int reviewId, String attachmentUri) throws DAOException {

        String reviewSubjectURI = user.getReviewUri(reviewId);
        TripleDTO dto = new TripleDTO(reviewSubjectURI, Predicates.CR_HAS_ATTACHMENT, attachmentUri);
        dto.setSourceUri(reviewSubjectURI);
        DAOFactory.get().getDao(HelperDAO.class).deleteTriple(dto);
    }
}
