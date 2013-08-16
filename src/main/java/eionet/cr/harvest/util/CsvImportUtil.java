package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.Collection;
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
     *
     * @param subject Subject to be checked
     * @return true if table file (CSV/TSV)
     */
    public static boolean isSourceTableFile(SubjectDTO subject) {
        if (subject.getObject(Predicates.RDF_TYPE) != null) {
            return Subjects.CR_TABLE_FILE.equals(subject.getObjectValue(Predicates.RDF_TYPE));
        }
        return false;
    }

    /**
     * Harvests CSV/TSV file.
     *
     * @param subject Subject data object of file location.
     * @param uri file (Source/Graph) uri
     * @param userName user who executed the harvest
     * @return List of warning messages recieved from upload and post harvest scripts
     * @throws Exception if harvest fails
     */
    public static List<String> harvestTableFile(SubjectDTO subject, String uri, String userName) throws Exception {

        List<String> warningMessages = new ArrayList<String>();

        String fileUri = uri;
        String fileLabel = subject.getObjectValue(Predicates.RDFS_LABEL);
        FileType fileType = FileType.valueOf(subject.getObjectValue(Predicates.CR_MEDIA_TYPE));
        String objectsType = subject.getObjectValue(Predicates.CR_OBJECTS_TYPE);
        String publisher = subject.getObjectValue(Predicates.DCTERMS_PUBLISHER);
        String license = subject.getObjectValue(Predicates.DCTERMS_RIGHTS);
        String attribution = subject.getObjectValue(Predicates.DCTERMS_BIBLIOGRAPHIC_CITATION);
        String source = subject.getObjectValue(Predicates.DCTERMS_SOURCE);
        long fileSize = Long.parseLong(subject.getObjectValue(Predicates.CR_BYTE_SIZE));
        Collection<String> uniqueColumns = subject.getObjectValues(Predicates.CR_OBJECTS_UNIQUE_COLUMN);

        String folderUri = StringUtils.substringBeforeLast(uri, "/");
        String relativeFilePath = FolderUtil.extractPathInUserHome(fileUri);

        // Clear graph
        DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(Collections.singletonList(uri));

        CsvImportHelper helper =
                new CsvImportHelper(new ArrayList<String>(uniqueColumns), fileUri, fileLabel, fileType, objectsType, publisher,
                        license, attribution, source);

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
