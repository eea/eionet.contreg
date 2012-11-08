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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.BrowseVoidDatasetsDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.readers.VoidDatasetsReader;
import eionet.cr.dao.util.VoidDatasetsResultRow;
import eionet.cr.util.Bindings;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * Virtuoso-specific implementation of {@link BrowseVoidDatasetsDAO}.
 *
 * @author jaanus
 */
public class VirtuosoBrowseVoidDatasetsDAO extends VirtuosoBaseDAO implements BrowseVoidDatasetsDAO {

    /**
     * @see eionet.cr.dao.BrowseVoidDatasetsDAO#findDatasets(java.util.List, java.util.List)
     */
    @Override
    public List<VoidDatasetsResultRow> findDatasets(List<String> creators, List<String> subjects) throws DAOException {

        Bindings bindings = new Bindings();

        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX dcterms: <http://purl.org/dc/terms/>\n");
        sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
        sb.append("PREFIX void: <http://rdfs.org/ns/void#>\n");
        sb.append("\n");
        sb.append("SELECT ?dataset ?label ?creator sql:group_concat(?subject,', ') AS ?subjects\n");
        sb.append("WHERE {\n");
        sb.append("  ?dataset a void:Dataset .\n");
        sb.append("  ?dataset dcterms:title ?label.\n");
        sb.append("  ?dataset dcterms:creator ?ucreator .\n");
        sb.append("  ?ucreator rdfs:label ?creator.\n");

        if (subjects != null && !subjects.isEmpty()) {
            sb.append("  ?dataset dcterms:subject ?usubject .\n");
            sb.append("  ?usubject rdfs:label ?subject.\n");
        }

        sb.append("  FILTER (LANG(?label) IN ('en','')).\n");

        if (creators != null && !creators.isEmpty()) {
            // sb.append("  FILTER (?creator = 'Eurostat').\n");
            sb.append("  FILTER (?creator IN (").append(variablesCSV("crt", creators.size())).append("))\n");
            for (int i = 0; i < creators.size(); i++) {
                bindings.setString("crt" + (i + 1), creators.get(i));
            }
        }

        if (subjects != null && !subjects.isEmpty()) {
            sb.append("  FILTER (?subject IN (").append(variablesCSV("sbj", subjects.size())).append("))\n");
            for (int i = 0; i < subjects.size(); i++) {
                bindings.setString("sbj" + (i + 1), subjects.get(i));
            }
        } else {
            sb.append("  OPTIONAL {?dataset dcterms:subject ?usubject .\n");
            sb.append("           ?usubject rdfs:label ?subject }\n");
        }
        sb.append("}\n");

        List<VoidDatasetsResultRow> datasets = executeSPARQL(sb.toString(), bindings, new VoidDatasetsReader());
        return datasets;
    }

    /**
     * @see eionet.cr.dao.BrowseVoidDatasetsDAO#findCreators(java.util.List)
     */
    @Override
    public List<String> findCreators(List<String> subjects) throws DAOException {

        Bindings bindings = new Bindings();

        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX dcterms: <http://purl.org/dc/terms/>\n");
        sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
        sb.append("PREFIX void: <http://rdfs.org/ns/void#>\n");
        sb.append("\n");
        sb.append("SELECT distinct ?creator\n");
        sb.append("WHERE {\n");
        sb.append("  ?dataset a void:Dataset .\n");
        sb.append("  ?dataset dcterms:title ?label.\n");
        sb.append("  ?dataset dcterms:creator ?ucreator .\n");
        sb.append("  ?ucreator rdfs:label ?creator.\n");

        if (subjects != null && !subjects.isEmpty()) {

            sb.append("  ?dataset dcterms:subject ?usubject .\n");
            sb.append("  ?usubject rdfs:label ?subject.\n");
            sb.append("  FILTER (?subject IN (").append(variablesCSV("sbj", subjects.size())).append("))\n");

            for (int i = 0; i < subjects.size(); i++) {
                bindings.setString("sbj" + (i + 1), subjects.get(i));
            }
        }

        sb.append("  FILTER (LANG(?label) IN ('en','')).\n");
        sb.append("}");

        List<String> result = executeSPARQL(sb.toString(), bindings, new SingleObjectReader<String>());
        return result;
    }

    /**
     * @see eionet.cr.dao.BrowseVoidDatasetsDAO#findSubjects(java.util.List)
     */
    @Override
    public List<String> findSubjects(List<String> creators) throws DAOException {

        Bindings bindings = new Bindings();

        StringBuilder sb = new StringBuilder();
        sb.append("PREFIX dcterms: <http://purl.org/dc/terms/>\n");
        sb.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
        sb.append("PREFIX void: <http://rdfs.org/ns/void#>\n");
        sb.append("\n");
        sb.append("SELECT distinct ?subject\n");
        sb.append("WHERE {\n");
        sb.append("  ?dataset a void:Dataset .\n");
        sb.append("  ?dataset dcterms:title ?label.\n");
        sb.append("  ?dataset dcterms:creator ?ucreator .\n");
        sb.append("  ?ucreator rdfs:label ?creator.\n");
        sb.append("  ?dataset dcterms:subject ?usubject .\n");
        sb.append("  ?usubject rdfs:label ?subject.\n");
        sb.append("  FILTER (LANG(?label) IN ('en','')).\n");
        sb.append("  FILTER (LANG(?subject) IN ('en','')).\n");

        if (creators != null && !creators.isEmpty()) {
            sb.append("  FILTER (?creator IN (").append(variablesCSV("crt", creators.size())).append("))\n");
            for (int i = 0; i < creators.size(); i++) {
                bindings.setString("crt" + (i + 1), creators.get(i));
            }
        }

        sb.append("}");

        List<String> result = executeSPARQL(sb.toString(), bindings, new SingleObjectReader<String>());
        return result;
    }

    /**
     *
     * @param args
     * @throws DAOException
     */
    public static void main(String[] args) throws DAOException {

        ArrayList<String> creators = new ArrayList<String>();
        creators.add("Eurostat");
        //creators.add("European Environment Agency");

        ArrayList<String> subjects = new ArrayList<String>();
        //subjects.add("Population and social conditions");
        subjects.add("Business surveys - NACE Rev. 1.1");

        BrowseVoidDatasetsDAO dao = DAOFactory.get().getDao(BrowseVoidDatasetsDAO.class);
        List<VoidDatasetsResultRow> searchVoidDatasets = dao.findDatasets(creators, subjects);
        System.out.println(searchVoidDatasets.size());

        List<String> distinctCreators = dao.findCreators(subjects);
        System.out.println(distinctCreators);

        List<String> distinctSubjects = dao.findSubjects(creators);
        System.out.println(distinctSubjects.size());
    }

}
