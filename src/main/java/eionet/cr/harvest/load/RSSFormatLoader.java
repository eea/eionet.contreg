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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eionet.cr.common.Predicates;

/**
 * Implementation of {@link ContentLoader} for the content in RRS/Atom format.
 * 
 * @author Jaanus Heinlaid
 */
public class RSSFormatLoader implements ContentLoader {

    /**
     * @throws IOException
     * @throws FeedException
     * @see eionet.cr.harvest.load.ContentLoader#load(java.io.InputStream, org.openrdf.repository.RepositoryConnection,
     *      java.sql.Connection, java.lang.String, java.lang.String)
     */
    @Override
    public int load(InputStream inputStream, RepositoryConnection repoConn, Connection sqlConn, String baseUri, String contextUri)
            throws IOException, OpenRDFException, ContentParsingException {

        SyndFeedInput input = new SyndFeedInput();
        try {
            SyndFeed feed = input.build(new XmlReader(inputStream));

            Resource context = repoConn.getValueFactory().createURI(contextUri);
            int tripleCount = saveFeedMetadata(feed, repoConn, context);

            List feedItems = feed.getEntries();
            for (Iterator iterator = feedItems.iterator(); iterator.hasNext();) {

                SyndEntry item = (SyndEntry) iterator.next();
                tripleCount += saveFeedItemMetadata(item, feed);
            }

            return tripleCount;

        } catch (FeedException e) {
            throw new ContentParsingException(e.getMessage(), e);
        }
    }

    /**
     * 
     * @param feed
     * @throws RepositoryException
     */
    private int saveFeedMetadata(SyndFeed feed, RepositoryConnection repoConn, Resource context) throws RepositoryException {

        ArrayList<Statement> statements = new ArrayList<Statement>();
        ValueFactory vf = repoConn.getValueFactory();
        URI feedURI = vf.createURI(feed.getUri());
        Statement statement;

        String feedTitle = feed.getTitle(); // to be mapped into dcterms:title and rdfs:label

        if (StringUtils.isNotBlank(feedTitle)) {
            statement = vf.createStatement(feedURI, vf.createURI(Predicates.DCTERMS_TITLE), vf.createLiteral(feedTitle));
            statements.add(statement);
        }

        List feedAuthors = feed.getAuthors(); // to be mapped into dcterms:creator
        for (Iterator iterator = feedAuthors.iterator(); iterator.hasNext();) {
            Object author = iterator.next();
            if (author instanceof SyndPerson) {
                SyndPerson person = (SyndPerson) author;
                String personUri = person.getUri();
                if (StringUtils.isNotBlank(personUri)) {
                }
            } else if (author != null) {

            }
        }

        List feedCategories = feed.getCategories(); // to be mapped into dcterms:subject
        List feedContributors = feed.getContributors(); // to be mapped into dcterms:contributor
        String feedDescription = feed.getDescription(); // to be mapped into dcterms:abstract
        String feedEncoding = feed.getEncoding(); // to be mapped into cr:charset
        String feedLanguage = feed.getLanguage(); // to be mapped into dcterms:language
        Date feedPublishedDate = feed.getPublishedDate(); // to be mapped into dcterms:date

        repoConn.add(statements, context);
        return statements.size();
    }

    /**
     * 
     * @param item
     */
    private int saveFeedItemMetadata(SyndEntry item, SyndFeed feed) {

        String itemUri = item.getUri();
        String itemTitle = item.getTitle();

        List itemAuthors = item.getAuthors();
        List itemCategories = item.getCategories();
        List itemContributors = item.getContributors();
        SyndContent itemDescription = item.getDescription();
        Date itemPublishedDate = item.getPublishedDate();
        Date itemUpdatedDate = item.getUpdatedDate();
        SyndFeed itemSource = item.getSource();

        // TODO save the above metadata
        return 0;
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
