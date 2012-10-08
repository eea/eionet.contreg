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
 *        Juhan Voolaid
 */

package eionet.cr.dao.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryException;

import au.com.bytecode.opencsv.CSVReader;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.action.UploadCSVActionBean.FileType;
import eionet.cr.web.util.CharsetToolkit;

/**
 * Helper methods for importing CSV file.
 *
 * @author Juhan Voolaid
 */
public class CsvImportHelper {

    /** Column name for empty name. */
    public static final String EMPTY_COLUMN = "Empty";

    /** Columns detected in the uploaded file (it's the titles of the columns). */
    private List<String> columns;

    /** Column labels detected in the uploaded file (titles without type and language code). */
    private List<String> columnLabels;

    /** The column (i.e. column title) representing the contained objects' labels. */
    private String labelColumn;

    /** The columns (i.e. column titles) forming the contained objects' unique identifiers. */
    private List<String> uniqueColumns;

    /** The URI that will be assigned to the resource representing the file. */
    private String fileUri;

    /** User can specify rdf:label for the file. */
    private String fileLabel;

    /** Uploaded file's type. */
    private FileType fileType;

    /** The type of objects contained in the file (user-given free text). */
    private String objectsType;

    /** Publisher of uploaded material. */
    private String publisher;

    /** License of uploaded material. */
    private String license;

    /** Attribution. */
    private String attribution;

    /** Source of the uploaded material. */
    private String source;

    /**
     * @param guessEncoding
     * @return
     * @throws IOException
     */
    public CSVReader createCSVReader(String folderUri, String relativeFilePath, String userName,
            boolean guessEncoding) throws IOException {

        CSVReader result = null;
        // File file = FileStore.getInstance(getUserName()).getFile(relativeFilePath);
        File file = FileStore.getInstance(FolderUtil.getUserDir(folderUri, userName)).getFile(relativeFilePath);
        if (file != null && file.exists()) {
            if (guessEncoding) {
                Charset charset = CharsetToolkit.guessEncoding(file, 4096, Charset.forName("UTF-8"));
                result = new CSVReader(new InputStreamReader(new FileInputStream(file), charset), getDelimiter());
            } else {
                result = new CSVReader(new FileReader(file), getDelimiter());
            }
        }

        return result;
    }

    public void extractObjects(CSVReader csvReader) throws IOException, DAOException, RepositoryException {

        // Set columns and columnLabels by reading the first line.
        String[] columnsArray = csvReader.readNext();
        if (columnsArray == null || columnsArray.length == 0) {
            columns = new ArrayList<String>();
            columnLabels = new ArrayList<String>();
        } else {
            // We do trimming, because CSV Reader doesn't do it
            columns = Arrays.asList(trimAll(columnsArray));
            if (columns != null && columns.size() > 0) {
                columnLabels = new ArrayList<String>();
                int emptyColCount = 1;
                for (String col : columns) {
                    if (StringUtils.isEmpty(col)) {
                        col = EMPTY_COLUMN + emptyColCount++;
                    }
                    col = StringUtils.substringBefore(col, ":");
                    col = StringUtils.substringBefore(col, "@");
                    columnLabels.add(col.trim());
                }
            }
        }

        // Read the contained objects by reading the rest of lines.
        String[] line = null;
        String objectsTypeUri = fileUri + "/" + objectsType;
        HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);

        while ((line = csvReader.readNext()) != null) {
            SubjectDTO subject = extractObject(line, objectsTypeUri);
            helperDao.addTriples(subject);
        }

        // Construct a SPARQL query and store it as a property
        StringBuilder query = new StringBuilder();
        query.append("PREFIX tableFile: <" + fileUri + "#>\n\n");
        query.append("SELECT * FROM <").append(fileUri).append("> WHERE { \n");
        for (String column : columnLabels) {
            column = column.replace(" ", "_");
            String columnUri = "tableFile:" + column;
            query.append(" ?").append(objectsType).append(" ").append(columnUri).append(" ?").append(column).append(" . \n");
        }
        query.append("}");

        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_SPARQL_QUERY, ObjectDTO.createLiteral(query.toString()));

        // Finally, make sure that the file has the correct number of harvested statements in its predicates.
        DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatementsTriple(fileUri);
    }

    public void saveWizardInputs() throws DAOException, RepositoryException, IOException {

        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_OBJECTS_TYPE, ObjectDTO.createLiteral(objectsType));
        if (StringUtils.isNotEmpty(labelColumn)) {
            dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_OBJECTS_LABEL_COLUMN, ObjectDTO.createLiteral(labelColumn));
        }
        if (StringUtils.isNotEmpty(fileLabel)) {
            dao.insertUpdateSourceMetadata(fileUri, Predicates.RDFS_LABEL, ObjectDTO.createLiteral(fileLabel));
        }

        ObjectDTO[] uniqueColTitles = new ObjectDTO[uniqueColumns.size()];
        for (int i = 0; i < uniqueColumns.size(); i++) {
            uniqueColTitles[i] = ObjectDTO.createLiteral(uniqueColumns.get(i));
        }

        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_OBJECTS_UNIQUE_COLUMN, uniqueColTitles);

        // Copyright information
        if (StringUtils.isNotEmpty(publisher)) {
            if (StringUtils.startsWithIgnoreCase(publisher, "http")) {
                dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_PUBLISHER, ObjectDTO.createResource(publisher));
            } else {
                dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_PUBLISHER, ObjectDTO.createLiteral(publisher));
            }
        }
        if (StringUtils.startsWithIgnoreCase(license, "http")) {
            dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_LICENSE, ObjectDTO.createResource(license));
        } else {
            dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_RIGHTS, ObjectDTO.createLiteral(license));
        }
        if (StringUtils.isNotEmpty(attribution)) {
            dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_BIBLIOGRAPHIC_CITATION,
                    ObjectDTO.createLiteral(attribution));
        }
        if (StringUtils.isNotEmpty(source)) {
            if (StringUtils.startsWithIgnoreCase(source, "http")) {
                dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_SOURCE, ObjectDTO.createResource(source));
            } else {
                dao.insertUpdateSourceMetadata(fileUri, Predicates.DCTERMS_SOURCE, ObjectDTO.createLiteral(source));
            }
        }
    }

    private SubjectDTO extractObject(String[] line, String objectsTypeUri) {

        // Construct subject URI and DTO object.
        String subjectUri = fileUri + "/" + extractObjectId(line);
        SubjectDTO subject = new SubjectDTO(subjectUri, false);

        // Add rdf:type to DTO.
        ObjectDTO typeObject = new ObjectDTO(objectsTypeUri, false);
        typeObject.setSourceUri(fileUri);
        subject.addObject(Predicates.RDF_TYPE, typeObject);

        // Add all other values.
        for (int i = 0; i < columns.size(); i++) {

            // If current columns index out of bounds for some reason, then break.
            if (i >= line.length) {
                break;
            }

            // Get column title, skip this column if it's the label column, otherwise replace spaces.
            String column = columns.get(i);

            // Extract column type and language code
            String type = StringUtils.substringAfter(column, ":");
            if (type != null && type.length() == 0) {
                type = null;
            }
            String lang = StringUtils.substringAfter(column, "@");
            if (lang != null && lang.length() == 0) {
                lang = null;
            }

            // Get column label
            column = columnLabels.get(i);
            column = column.replace(" ", "_");

            // Create ObjectDTO representing the given column's value on this line
            ObjectDTO objectDTO = createValueObject(column, line[i], type, lang);
            objectDTO.setSourceUri(fileUri);

            // Add ObjectDTO to the subject.
            String predicateUri = fileUri + "#" + column;
            subject.addObject(predicateUri, objectDTO);

            // If marked as label column, add label property as well
            if (column.equals(labelColumn)) {
                subject.addObject(Predicates.RDFS_LABEL, objectDTO);
            }
        }

        return subject;
    }

    private String extractObjectId(String[] line) {

        StringBuilder buf = new StringBuilder();
        if (uniqueColumns != null && !uniqueColumns.isEmpty()) {

            for (String uniqueCol : uniqueColumns) {
                int colIndex = columnLabels.indexOf(uniqueCol);
                if (colIndex >= 0 && colIndex < line.length && !StringUtils.isBlank(line[colIndex])) {
                    if (buf.length() > 0) {
                        buf.append("_");
                    }
                    buf.append(line[colIndex]);
                }
            }
        }

        return buf.length() == 0 ? UUID.randomUUID().toString() : buf.toString();
    }

    private ObjectDTO createValueObject(String column, String value, String type, String lang) {

        HashMap<String, URI> types = new HashMap<String, URI>();
        types.put("url", null);
        types.put("uri", null);
        types.put("date", XMLSchema.DATE);
        types.put("datetime", XMLSchema.DATETIME);
        types.put("boolean", XMLSchema.BOOLEAN);
        types.put("integer", XMLSchema.INTEGER);
        types.put("int", XMLSchema.INT);
        types.put("long", XMLSchema.LONG);
        types.put("double", XMLSchema.DOUBLE);
        types.put("decimal", XMLSchema.DECIMAL);
        types.put("float", XMLSchema.FLOAT);

        // If type is not defined, but column name matches one of the types, then use column name as datatype
        if (type == null) {
            if (types.keySet().contains(column.toLowerCase())) {
                type = column.toLowerCase();
            }
        }

        ObjectDTO objectDTO = null;
        if (!StringUtils.isBlank(type)) {
            if (type.equalsIgnoreCase("url") || type.equalsIgnoreCase("uri")) {
                objectDTO = new ObjectDTO(value, lang, false, false, null);
            } else if (types.keySet().contains(type.toLowerCase())) {
                if (type.equalsIgnoreCase("boolean")) {
                    value = value.equalsIgnoreCase("true") ? "true" : "false";
                }
                URI datatype = types.get(type.toLowerCase());
                objectDTO = new ObjectDTO(value, lang, true, false, datatype);
            } else if (type.equalsIgnoreCase("number")) {
                try {
                    Integer.parseInt(value);
                    objectDTO = new ObjectDTO(value, lang, true, false, XMLSchema.INTEGER);
                } catch (NumberFormatException nfe1) {
                    try {
                        Long.parseLong(value);
                        objectDTO = new ObjectDTO(value, lang, true, false, XMLSchema.LONG);
                    } catch (NumberFormatException nfe2) {
                        try {
                            Double.parseDouble(value);
                            objectDTO = new ObjectDTO(value, lang, true, false, XMLSchema.DOUBLE);
                        } catch (NumberFormatException nfe3) {
                            // No need to throw or log it.
                        }
                    }
                }
            }
        }

        return objectDTO == null ? new ObjectDTO(value, lang, true, false, null) : objectDTO;
    }

    public char getDelimiter() {
        return fileType != null && fileType.equals(FileType.TSV) ? '\t' : ',';
    }

    public String[] trimAll(String[] strings) {

        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].trim();
            }
        }

        return strings;
    }
}
