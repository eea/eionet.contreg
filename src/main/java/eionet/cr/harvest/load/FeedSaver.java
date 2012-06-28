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
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.TextInput;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndImage;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.util.sesame.SesameUtil;

/**
 * Helper class that reads a feed (e.g. RSS, Atom) from the given input stream and saves it into the given context (i.e. graph) in
 * the given repository. Repository connections and the context are given via constructor, the stream is given via
 * {@link #save(InputStream)} method.
 *
 * @author Jaanus Heinlaid
 */
public class FeedSaver {

    /** */
    private static final Logger LOGGER = Logger.getLogger(FeedSaver.class);

    /** Connections to the repository and SQL database where the content is persisted into. */
    private RepositoryConnection repoConn;
    private Connection sqlConn;

    /** The graph where the triples will be loaded into. */
    private Resource context;

    /** The OpenRDF ValueFactory obtained from {@link #repoConn}. */
    private ValueFactory valueFactory;

    /** Number of triples saved. */
    private int triplesSaved;

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
        // This will preserve the original WireFeed, so we can later get it with SyndFeed.originalWireFeed().
        // See the latter's JavaDoc and other ROME docs for more info. Also look at this.saveTextInputMetadata(...).
        input.setPreserveWireFeed(true);

        try {
            // Read the feed from the given input stream.
            SyndFeed feed = input.build(new XmlReader(inputStream));
            String feedUri = getFeedUri(feed);

            // Save the read feed.
            saveFeedMetadata(feedUri, feed);

            // Loop through the feed's items, save them all and their relations to the feed as well.
            List feedItems = feed.getEntries();
            if (feedItems != null) {

                for (int i = 0; i < feedItems.size(); i++) {
                    SyndEntry item = (SyndEntry) feedItems.get(i);
                    String itemUri = getFeedItemUri(item);

                    // Save the feed item's metadata.
                    saveFeedItemMetadata(itemUri, item);

                    // Save the relation between the feed and the item.
                    saveItemToFeedRelation(feedUri, itemUri, i + 1);
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
     */
    private String getFeedUri(SyndFeed feed) {

        // Get the feed's URI, fall back to its link if URI could not be detected.
        // If no link could be detected either, fall back to the context URI.
        String feedUri = feed.getUri();
        if (!SesameUtil.isValidURI(feedUri, valueFactory)) {
            feedUri = feed.getLink();
        }
        if (!SesameUtil.isValidURI(feedUri, valueFactory)) {
            LOGGER.warn("Could not detect the feed's URI neither from getUri() nor from getLink(), falling back to context URI!");
            feedUri = context.stringValue();
        }
        return feedUri;
    }

    /**
     *
     * @param item
     * @return
     */
    private String getFeedItemUri(SyndEntry item) {

        // Get the item's URI, fall back to its link if URI could not be detected.
        String itemUri = item.getUri();
        if (!SesameUtil.isValidURI(itemUri, valueFactory)) {
            itemUri = item.getLink();
        }
        return itemUri;
    }

    /**
     * @param feedUri
     * @param feed
     * @return
     * @throws OpenRDFException
     */
    private void saveFeedMetadata(String feedUri, SyndFeed feed) throws OpenRDFException {

        // Save the feed's rdf:type.
        saveResourceTriple(feedUri, Predicates.RDF_TYPE, Subjects.RSSNG_CHANNEL_CLASS);

        // Save the feed's link.
        saveResourceTriple(feedUri, Predicates.RSSNG_LINK, feed.getLink());

        // The feed's title mapped into rssng:title and rdfs:label.
        String feedTitle = feed.getTitle();
        String feedTitleNormalized = stripOfHtml(feedTitle);
        saveLiteralTriple(feedUri, Predicates.RSSNG_TITLE, feedTitleNormalized);
        saveLiteralTriple(feedUri, Predicates.RDFS_LABEL, feedTitleNormalized);

        // The feed's authors mapped into dcterms:creator.
        savePersons(feedUri, Predicates.DCTERMS_CREATOR, feed.getAuthors());

        // The feed's description mapped into rssng:abstract.
        saveLiteralTriple(feedUri, Predicates.RSSNG_SUMMARY, stripOfHtml(feed.getDescription()));

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

        // Save the feed image's metadata.
        saveFeedImageMetadata(feedUri, feed);

        // Save the feed's text input metadata.
        saveTextInputMetadata(feedUri, feed);
    }

    /**
     *
     * @param feedUri
     * @param feed
     * @throws RepositoryException
     */
    private void saveFeedImageMetadata(String feedUri, SyndFeed feed) throws RepositoryException{

        SyndImage feedImage = feed.getImage();

        // The image object must not be null, and its URL must not be blank.
        if (feedImage == null || StringUtils.isBlank(feedImage.getUrl())){
            return;
        }

        String imageUrl = feedImage.getUrl();

        // Save the feed image's rdf:type.
        saveResourceTriple(imageUrl, Predicates.RDF_TYPE, Subjects.RSSNG_IMAGE_CLASS);

        // The image's URl attribute, goes into rssng:url.
        saveResourceTriple(imageUrl, Predicates.RSSNG_URL, imageUrl);

        // Relation between the feed and the image.
        saveResourceTriple(feedUri, Predicates.RSSNG_IMAGE, imageUrl);

        // Title goes into rssng:title and rdfs:label.
        String titleNormalized = stripOfHtml(feedImage.getTitle());
        saveLiteralTriple(imageUrl, Predicates.RSSNG_TITLE, titleNormalized);
        saveLiteralTriple(imageUrl, Predicates.RDFS_LABEL, titleNormalized);

        // Description goes into rssng:summary.
        saveLiteralTriple(imageUrl, Predicates.RSSNG_SUMMARY, stripOfHtml(feedImage.getDescription()));

        // The image's link goes into rssng:link.
        saveLiteralTriple(imageUrl, Predicates.RSSNG_LINK, feedImage.getLink());
    }

    /**
     *
     * @param feedUri
     * @param feed
     * @throws RepositoryException
     */
    private void saveTextInputMetadata(String feedUri, SyndFeed feed) throws RepositoryException{

        // SyndFeed itself does not return textInput metadata, because it is a normalization of teh various RSS/Atom formats,
        // and not all of them have a TextInput. So we get the original WireFeed object that this SyndFeed was created from,
        // and see if we can get a TextInput from there. For this, we had to call SyndFeedInput.setPreserveWireFeed(true)
        // earlier on, so that the original WireFeed is preserved indeed.

        WireFeed originalWireFeed = feed.originalWireFeed();
        if (originalWireFeed == null || !(originalWireFeed instanceof Channel)){
            return;
        }

        TextInput textInput = ((Channel) originalWireFeed).getTextInput();
        // The input must not be null and its link must not be blank.
        if (textInput == null || StringUtils.isBlank(textInput.getLink())){
            return;
        }

        String inputLink = textInput.getLink();

        // Save the text input's rdf:type.
        saveResourceTriple(inputLink, Predicates.RDF_TYPE, Subjects.RSSNG_TEXTINPUT_CLASS);

        // Save the input's link.
        saveResourceTriple(inputLink, Predicates.RSSNG_LINK, textInput.getLink());

        // Relation between the feed and the text input.
        saveResourceTriple(feedUri, Predicates.RSSNG_TEXTINPUT, inputLink);

        // Title goes into rssng:title and rdfs:label.
        String titleNormalized = stripOfHtml(textInput.getTitle());
        saveLiteralTriple(inputLink, Predicates.RSSNG_TITLE, titleNormalized);
        saveLiteralTriple(inputLink, Predicates.RDFS_LABEL, titleNormalized);

        // Description goes into rssng:summary.
        saveLiteralTriple(inputLink, Predicates.RSSNG_SUMMARY, stripOfHtml(textInput.getDescription()));

        // The input's field name goes into rssng:name.
        saveLiteralTriple(inputLink, Predicates.RSSNG_NAME, textInput.getName());
    }

    /**
     * @param itemUri
     * @param item
     * @return
     * @throws RepositoryException
     */
    private void saveFeedItemMetadata(String itemUri, SyndEntry item) throws RepositoryException {

        // Save the item's rdf:type.
        saveResourceTriple(itemUri, Predicates.RDF_TYPE, Subjects.RSSNG_ANNOUNCEMENT_CLASS);

        // Save the item's link.
        saveResourceTriple(itemUri, Predicates.RSSNG_LINK, item.getLink());

        // Title mapped into rssng:title and rdfs:label.
        String itemTitle = item.getTitle();
        String itemTitleNormalized = stripOfHtml(itemTitle);
        saveLiteralTriple(itemUri, Predicates.RSSNG_TITLE, itemTitleNormalized);
        saveLiteralTriple(itemUri, Predicates.RDFS_LABEL, itemTitleNormalized);

        // Authors mapped into dcterms:creator.
        savePersons(itemUri, Predicates.DCTERMS_CREATOR, item.getAuthors());

        // Categories mapped into dcterms:subject.
        saveCategories(itemUri, Predicates.DCTERMS_SUBJECT, item.getCategories());

        // Contributors mapped into dcterms:contributor.
        savePersons(itemUri, Predicates.DCTERMS_CONTRIBUTOR, item.getContributors());

        // Description mapped into rssng:summary.
        SyndContent descriptionContent = item.getDescription();
        if (descriptionContent != null) {
            saveLiteralTriple(itemUri, Predicates.RSSNG_SUMMARY, stripOfHtml(descriptionContent.getValue()));
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

            boolean isValidTaxonomyUri = SesameUtil.isValidURI(taxonomyUri, valueFactory);
            if (isValidTaxonomyUri && StringUtils.isNotBlank(name)) {
                saveLiteralTriple(taxonomyUri, Predicates.RDFS_LABEL, name);
                saveResourceTriple(subjectUri, predicateUri, taxonomyUri);
            } else if (isValidTaxonomyUri) {
                saveResourceTriple(subjectUri, predicateUri, taxonomyUri);
            } else {
                saveLiteralTriple(subjectUri, predicateUri, name);
            }
        }
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

        saveResourceTriple(feedUri, Predicates.RSSNG_ITEM, itemUri);
        saveLiteralTriple(itemUri, Predicates.RSSNG_ORDER, Integer.valueOf(position));
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

        Literal literal = SesameUtil.createLiteral(object, valueFactory);
        if (literal != null) {
            saveTriple(subjectUri, predicateUri, literal);
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

        URI objectResource = null;
        try {
            objectResource = valueFactory.createURI(objectUri);
        } catch (IllegalArgumentException e) {
            // If the given object value could not be converted into OpenRDF URI.
            objectResource = null;
        }

        // In case we could not convert the object into resource, try saving it as a literal instead.
        if (objectResource != null) {
            saveTriple(subjectUri, predicateUri, objectResource);
        } else {
            saveLiteralTriple(subjectUri, predicateUri, objectUri);
        }
    }

    /**
     *
     * @param subjectUri
     * @param predicateUri
     * @param object
     * @throws RepositoryException
     */
    private void saveTriple(String subjectUri, String predicateUri, Value object) throws RepositoryException {

        URI subjectResource = null;
        try {
            subjectResource = valueFactory.createURI(subjectUri);
        } catch (IllegalArgumentException e) {
            // If the given subject URI string could not be converted into OpenRDF URI.
            subjectResource = null;
        }

        if (subjectResource != null) {
            repoConn.add(subjectResource, valueFactory.createURI(predicateUri), object, context);
            triplesSaved++;
        }
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
