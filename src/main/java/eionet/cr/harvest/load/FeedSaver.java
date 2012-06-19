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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest.load;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;

/**
 *
 * @author Jaanus Heinlaid
 */
public class FeedSaver {

    /** */
    private static final String RDF_SEQUENCE_ITEM_PREFIX = Namespace.RDF.getUri() + "_";

    /** */
    private RepositoryConnection repoConn;
    private Connection sqlConn;

    /** */
    private Resource context;

    /** */
    private ValueFactory valueFactory;

    /** Number of triples saved. */
    private int triplesSaved;

    /**
     * The resource representing the rdf:Seq of feed items. See RSS 1.0 examples from http://web.resource.org/rss/1.0/spec. Is
     * lazily instantiated below.
     */
    private Resource itemsBNode;

    /**
     * @param repoConn
     * @param sqlConn
     * @param contextUri
     */
    public FeedSaver(RepositoryConnection repoConn, Connection sqlConn, String contextUri) {
        this.repoConn = repoConn;
        this.sqlConn = sqlConn;
        this.valueFactory = repoConn.getValueFactory();
        this.context = this.valueFactory.createURI(contextUri);
    }

    /**
     * @param inputStream
     * @throws ContentParsingException
     * @throws IOException
     * @throws OpenRDFException
     *
     */
    public void save(InputStream inputStream) throws ContentParsingException, IOException, OpenRDFException {

        SyndFeedInput input = new SyndFeedInput();
        try {
            SyndFeed feed = input.build(new XmlReader(inputStream));
            saveFeedMetadata(feed);

            List feedItems = feed.getEntries();
            if (feedItems != null) {

                for (int i = 0; i < feedItems.size(); i++) {
                    SyndEntry item = (SyndEntry) feedItems.get(i);
                    saveFeedItemMetadata(item);
                    saveItemToFeedRelation(feed.getUri(), item.getUri(), i + 1);
                }
            }
        } catch (FeedException e) {
            throw new ContentParsingException(e.getMessage(), e);
        }
    }

    /**
     * @return the triplesSaved
     */
    public int getNumberOfTriplesSaved() {
        return triplesSaved;
    }

    /**
     * @param feed
     * @return
     * @throws OpenRDFException
     */
    private void saveFeedMetadata(SyndFeed feed) throws OpenRDFException {

        String feedUri = feed.getUri();
        saveResourceTriple(feedUri, Predicates.RDF_TYPE, Subjects.RSS_CHANNEL_CLASS);

        // The feed's title mapped into dcterms:title and rdfs:label.
        String feedTitle = feed.getTitle();
        String feedTitleNormalized = stripOfHtml(feedTitle);
        saveLiteralTriple(feedUri, Predicates.DCTERMS_TITLE, feedTitleNormalized);
        saveLiteralTriple(feedUri, Predicates.RDFS_LABEL, feedTitleNormalized);

        // The feed's authors mapped into dcterms:creator.
        savePersons(feedUri, Predicates.DCTERMS_CREATOR, feed.getAuthors());

        // The feed's description mapped into dcterms:abstract.
        saveLiteralTriple(feedUri, Predicates.DCTERMS_ABSTRACT, stripOfHtml(feed.getDescription()));

        // The feed's categories mapped into dcterms:subject.
        saveCategories(feedUri, Predicates.DCTERMS_SUBJECT, feed.getCategories());

        // The feed's contributors mapped into dcterms:contributor.
        savePersons(feedUri, Predicates.DCTERMS_CONTRIBUTOR, feed.getContributors());

        // The feed's published-date mapped into dcterms:date.
        saveLiteralTriple(feedUri, Predicates.DCTERMS_DATE, feed.getPublishedDate());

        // The feed's language mapped into dcterms:language.
        saveLiteralTriple(feedUri, Predicates.DCTERMS_LANGUAGE, feed.getLanguage());

        // The feed's charset encoding mapped into cr:charset.
        saveLiteralTriple(feedUri, Predicates.CR_CHARSET, feed.getEncoding());
    }

    /**
     * @param item
     * @return
     * @throws RepositoryException
     */
    private void saveFeedItemMetadata(SyndEntry item) throws RepositoryException {

        String itemUri = item.getUri();
        saveResourceTriple(itemUri, Predicates.RDF_TYPE, Subjects.RSS_ITEM_CLASS);

        // Title mapped into dcterms:title and rdfs:label.
        String itemTitle = item.getTitle();
        String itemTitleNormalized = stripOfHtml(itemTitle);
        saveLiteralTriple(itemTitle, Predicates.DCTERMS_TITLE, itemTitleNormalized);
        saveLiteralTriple(itemTitle, Predicates.RDFS_LABEL, itemTitleNormalized);

        // Authors mapped into dcterms:creator.
        savePersons(itemUri, Predicates.DCTERMS_CREATOR, item.getAuthors());

        // Categories mapped into dcterms:subject.
        saveCategories(itemUri, Predicates.DCTERMS_SUBJECT, item.getCategories());

        // Contributors mapped into dcterms:contributor.
        savePersons(itemUri, Predicates.DCTERMS_CONTRIBUTOR, item.getContributors());

        // Description mapped into dcterms:abstract.
        SyndContent descriptionContent = item.getDescription();
        if (descriptionContent != null) {
            saveLiteralTriple(itemUri, Predicates.DCTERMS_ABSTRACT, descriptionContent.getValue());
        }

        // Both published-date and updated-date mapped into dcterms:date
        saveLiteralTriple(itemUri, Predicates.DCTERMS_DATE, item.getPublishedDate());
        saveLiteralTriple(itemUri, Predicates.DCTERMS_DATE, item.getUpdatedDate());

        // The feed-item's 3rd party source mapped into dcterms:source.
        SyndFeed itemSource = item.getSource();
        if (itemSource != null) {
            saveResourceTriple(itemUri, Predicates.DCTERMS_SOURCE, itemSource.getUri());
        }
    }

    /**
     *
     * @param subjectUri
     * @param predicateUri
     * @param persons
     * @throws RepositoryException
     */
    private void savePersons(String subjectUri, String predicateUri, List persons) throws RepositoryException {

        if (persons == null || persons.isEmpty()) {
            return;
        }

        for (Iterator iterator = persons.iterator(); iterator.hasNext();) {

            Object author = iterator.next();
            if (author != null) {
                if (author instanceof SyndPerson) {

                    SyndPerson person = (SyndPerson) author;
                    String personUri = person.getUri();
                    if (StringUtils.isNotBlank(personUri)) {

                        saveSyndPerson(person, personUri);
                        saveResourceTriple(subjectUri, predicateUri, personUri);
                    }
                } else {
                    saveLiteralTriple(subjectUri, predicateUri, author.toString());
                }
            }
        }
    }

    /**
     * @param person
     * @param personUri
     * @throws RepositoryException
     */
    private void saveSyndPerson(SyndPerson person, String personUri) throws RepositoryException {

        saveResourceTriple(personUri, Predicates.RDF_TYPE, Subjects.FOAF_PERSON_CLASS);

        String personName = person.getName();
        if (StringUtils.isNotBlank(personName)) {
            saveLiteralTriple(personUri, Predicates.FOAF_NAME, personName);
        }

        String personEmail = person.getEmail();
        if (StringUtils.isNotBlank(personEmail)) {
            // From the RSS 2.0 specs it seems we can assume that e-mails are provided implicitly,
            // so lets make an SHA-1 checksum of them.
            saveLiteralTriple(personUri, Predicates.FOAF_MBOX_SHA1SUM, DigestUtils.shaHex(personEmail));
        }
    }

    /**
     * @param subjectUri
     * @param predicateUri
     * @param categories
     * @throws RepositoryException
     */
    private void saveCategories(String subjectUri, String predicateUri, List categories) throws RepositoryException {

        if (categories == null || categories.isEmpty()) {
            return;
        }

        for (Iterator iterator = categories.iterator(); iterator.hasNext();) {

            SyndCategory syndCategory = (SyndCategory) iterator.next();
            String taxonomyUri = syndCategory.getTaxonomyUri();
            String name = syndCategory.getName();

            if (StringUtils.isNotBlank(taxonomyUri) && StringUtils.isNotBlank(name)) {
                saveLiteralTriple(taxonomyUri, Predicates.RDFS_LABEL, name);
                saveResourceTriple(subjectUri, predicateUri, taxonomyUri);
            } else if (StringUtils.isNotBlank(taxonomyUri)) {
                saveResourceTriple(subjectUri, predicateUri, taxonomyUri);
            } else {
                saveLiteralTriple(subjectUri, predicateUri, name);
            }
        }
    }

    /**
     * @param object
     * @return
     */
    private Literal createLiteral(Object object) {

        Literal literal = null;
        if (object instanceof Date) {
            GregorianCalendar gregCalendar = new GregorianCalendar();
            gregCalendar.setTime((Date) object);
            try {
                XMLGregorianCalendar xmlGregCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCalendar);
                literal = valueFactory.createLiteral(xmlGregCalendar);
            } catch (DatatypeConfigurationException e) {
                throw new CRRuntimeException("Failed to instantiate XML datatype factory implementation", e);
            }
        } else {
            String objectString = object.toString();
            if (StringUtils.isNotEmpty(objectString)) {
                literal = valueFactory.createLiteral(objectString);
            }
        }
        return literal;
    }

    /**
     *
     * @param feedUri
     * @param itemUri
     * @param position
     * @throws RepositoryException
     */
    private void saveItemToFeedRelation(String feedUri, String itemUri, int position) throws RepositoryException {

        if (StringUtils.isBlank(feedUri) || StringUtils.isBlank(itemUri)) {
            return;
        }

        if (itemsBNode == null) {

            // The rdf:Seq of feed items is a blank node.
            itemsBNode = valueFactory.createBNode();

            // Save the triple that states the sequence's rdf:type.
            saveTriple(itemsBNode, valueFactory.createURI(Predicates.RDF_TYPE), valueFactory.createURI(Subjects.RDF_SEQ));

            // Save the triple that relates the feed to the items.
            saveTriple(valueFactory.createURI(feedUri), valueFactory.createURI(Predicates.RSS_ITEMS), itemsBNode);
        }

        // Save the item's presence in the items sequence.
        saveTriple(itemsBNode, valueFactory.createURI(RDF_SEQUENCE_ITEM_PREFIX + position), valueFactory.createURI(itemUri));
    }

    /**
     *
     * @param subjectUri
     * @param predicateUri
     * @param object
     * @throws RepositoryException
     */
    private void saveLiteralTriple(String subjectUri, String predicateUri, Object object) throws RepositoryException {

        if (StringUtils.isBlank(subjectUri) || StringUtils.isBlank(predicateUri) || object == null) {
            return;
        }

        Literal literal = createLiteral(object);
        if (literal != null) {
            saveTriple(valueFactory.createURI(subjectUri), valueFactory.createURI(predicateUri), literal);
            triplesSaved++;
        }
    }

    /**
     *
     * @param subjectUri
     * @param predicateUri
     * @param objectUri
     * @throws RepositoryException
     */
    private void saveResourceTriple(String subjectUri, String predicateUri, String objectUri) throws RepositoryException {

        if (StringUtils.isBlank(subjectUri) || StringUtils.isBlank(predicateUri) || StringUtils.isBlank(objectUri)) {
            return;
        }

        saveTriple(valueFactory.createURI(subjectUri), valueFactory.createURI(predicateUri), valueFactory.createURI(objectUri));
        triplesSaved++;
    }

    /**
     *
     * @param subject
     * @param predicate
     * @param object
     * @throws RepositoryException
     */
    private void saveTriple(Resource subject, URI predicate, Value object) throws RepositoryException {

        repoConn.add(subject, predicate, object, context);
        triplesSaved++;
    }

    /**
     * Removes any HTML content from the given text.
     *
     * @param text
     * @return
     */
    private String stripOfHtml(String text) {

        if (StringUtils.isBlank(text)) {
            return text;
        }

        // Using the Jericho HTML Parser for Java.
        return new Renderer(new Segment(new Source(text), 0, text.length())).toString();
    }
}
