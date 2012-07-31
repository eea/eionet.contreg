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
 * Jaanus Heinlaid
 */
package eionet.cr.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.ntriples.NTriplesParserFactory;

/**
 * Analyzer for N3 Triples files.
 * Contains methods to detect if the file is in NTriples format.
 */
public class N3Analysis {

    /** */
    private static Log logger = LogFactory.getLog(N3Analysis.class);
    /**
    *
    * @param file File to be analyzed
    * @param contextUrl context URL
    * @throws RDFHandlerException if handling N3 fails
    * @throws RDFParseException if parsing fails
    * @throws IOException is exception occurs at I/O level
    */
   public void parse(File file, String contextUrl) throws   IOException,
       RDFHandlerException, RDFParseException {

       FileInputStream inputStream = null;
       try {
           inputStream = new FileInputStream(file);
           logger.debug("Start N3 parsing");
           parse(inputStream, contextUrl);
           logger.debug("N3 parsing succeeded. Seems to be a N3 file");

       } finally {
           IOUtils.closeQuietly(inputStream);
       }
   }


   /**
    *
    * @param inputStream input stream of a harvest source
    * @param contextUrl context url
    * @throws RDFHandlerException if handling N3 fails
    * @throws RDFParseException if parsing fails
    * @throws IOException is exception occurs at I/O level
    */
   public void parse(InputStream inputStream, String contextUrl) throws RDFHandlerException, RDFParseException, IOException {
       NTriplesParserFactory factory = new NTriplesParserFactory();
       RDFParser parser = factory.getParser();
       ArrayList<Statement> statements = new ArrayList<Statement>();
       N3Handler  collector = new N3Handler(statements);
       parser.setRDFHandler(collector);

       parser.parse(inputStream, contextUrl);
   }

   /**
    * Internal N3 handler to aviod Out of memory errors.
    * @author kaido
    */
   private class N3Handler extends StatementCollector {
       /** internal statements counter. */
       int counter = 100;

       /**
        * Initializes Statement handler.
        * @param statements statements collection not used
        */
       N3Handler(Collection<Statement> statements) {
           super(statements);
       }
       @Override
       public void handleStatement(Statement st) {
           counter++;
           //check only first 100 statements
           //StatemenCollector adds statments to the collection that may cause OutOfMemory while processing large files
           if (counter <= 100) {
               super.handleStatement(st);
           }
       }

   }

//   public static void main(String[] args) {
//
//       N3Analysis info = new N3Analysis();
//       try {
//           /*
//           String fileName = "c:/temp/long-test.n3";
//            String fileName = "c:/temp/test.n3";
//            */
//           String fileName = "c:/temp/paha.n3";
//           info.parse(new File(fileName), "http://localhost:8080/cr/harvester");
//       } catch (Throwable t) {
//           t.printStackTrace();
//       }
//   }

}
