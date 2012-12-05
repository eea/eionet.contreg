package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.helpers.CsvImportHelper;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.action.UploadCSVActionBean.FileType;

/**
 * Util class for CSV/TSV files import.
 *
 * @author kaido
 */
public final class CsvImportUtil {

    /** */
    private static final Logger LOGGER = Logger.getLogger(CsvImportUtil.class);

    /**
     * to prevent initialization.
     */
    private CsvImportUtil() {

    }

    /**
     * Checks if the given source is a table file.
     * @param subject Subject to be checked
     * @return true if table file (CSV/TSV)
     */
    public static boolean isSourceTableFile(SubjectDTO subject) {
        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            return Subjects.CR_TABLE_FILE.equals(subject.getObject(Predicates.RDF_TYPE).getValue());
        }
        return false;
    }

    /**
     * Returns object Value.
     * @param subject Subject data object
     * @param predicate predicate value
     * @return Object value, null if no object for this predicate
     */
    private static String getObjectValue(SubjectDTO subject, String predicate) {
        if (subject.getObject(predicate) != null) {
            return subject.getObject(predicate).getValue();
        }
        return null;
    }


    /**
     * Harvests CSV/TSV file.
     * @param subject Subject data object of file location.
     * @param uri file (Source/Graph) uri
     * @param userName user who executed the harvest
     * @return List of warning messages recieved from upload and post harvest scripts
     * @throws Exception if harvest fails
     */
    public static List<String> harvestTableFile(SubjectDTO subject, String uri, String userName) throws Exception {

        List<String> warningMessages = new ArrayList<String>();

        String fileUri = uri;
        String fileLabel = getObjectValue(subject, Predicates.RDFS_LABEL);
        FileType fileType = FileType.valueOf(getObjectValue(subject, Predicates.CR_MEDIA_TYPE));
        String objectsType = getObjectValue(subject, Predicates.CR_OBJECTS_TYPE);
        String publisher = getObjectValue(subject, Predicates.DCTERMS_PUBLISHER);
        String license = getObjectValue(subject, Predicates.DCTERMS_RIGHTS);
        String attribution = getObjectValue(subject, Predicates.DCTERMS_BIBLIOGRAPHIC_CITATION);
        String source = getObjectValue(subject, Predicates.DCTERMS_SOURCE);
        long fileSize = Long.parseLong(getObjectValue(subject, Predicates.CR_BYTE_SIZE));

        List<String> uniqueColumns = new ArrayList<String>();
        String uniqueColumnsString = getObjectValue(subject, Predicates.CR_OBJECTS_UNIQUE_COLUMN);
        if (StringUtils.isNotEmpty(uniqueColumnsString)) {
            String[] uniqueColumnsArr = subject.getObject(Predicates.CR_OBJECTS_UNIQUE_COLUMN).getValue().split(",");
            uniqueColumns = Arrays.asList(uniqueColumnsArr);
        }

        String folderUri = StringUtils.substringBeforeLast(uri, "/");
        String relativeFilePath = FolderUtil.extractPathInUserHome(fileUri);

        // Clear graph
        DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(Collections.singletonList(uri));

        CsvImportHelper helper =
                new CsvImportHelper(uniqueColumns, fileUri, fileLabel, fileType, objectsType, publisher, license,
                        attribution, source);

        // Store file as new source, but don't harvest it
        helper.insertFileMetadataAndSource(fileSize, userName);

        // Add metadata about user folder update
        helper.linkFileToFolder(folderUri, userName);

        // Parse and insert triples from file to triplestore
        CSVReader csvReader = helper.createCSVReader(folderUri, relativeFilePath, userName, true);

        try {
            csvReader = helper.createCSVReader(folderUri, relativeFilePath, userName, true);
            helper.extractObjects(csvReader);
            helper.saveWizardInputs();

            // Run data linking scripts
            try {
                List<String> warnings = helper.runScripts();
                if (warnings.size() > 0) {
                    for (String w : warnings) {
                        warningMessages.add(w);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to run data linking scripts", e);
                warningMessages.add("Failed to run data linking scripts: " + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Exception while reading uploaded file", e);
            warningMessages.add("Exception while reading uploaded file: " + e.getMessage());
        } finally {
            CsvImportHelper.close(csvReader);
        }

        return warningMessages;
    }

}
