package eionet.cr.harvest.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


import au.com.bytecode.opencsv.CSVReader;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.helpers.CsvImportHelper;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.action.UploadCSVActionBean.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Util class for CSV/TSV files import.
 *
 * @author kaido
 */
public final class CsvImportUtil {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvImportUtil.class);

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
     * Harvests a table file (i.e. a CSV/TSV file).
     *
     * @param subject {@link SubjectDTO} representing the file to be harvested. Must not be null!
     * @param userName User account name of the user requesting the harvest.
     * @return List of warning messages received from upload and post harvest scripts.
     * @throws Exception if harvest fails.
     */
    public static List<String> harvestTableFile(SubjectDTO subject, String userName) throws Exception {

        if (subject == null) {
            throw new IllegalArgumentException("The subject must not be null!");
        }

        List<String> warningMessages = new ArrayList<String>();

        String fileUri = subject.getUri();
        String fileLabel = subject.getObjectValue(Predicates.RDFS_LABEL);
        FileType fileType = FileType.valueOf(subject.getObjectValue(Predicates.CR_MEDIA_TYPE));
        String objectsType = subject.getObjectValue(Predicates.CR_OBJECTS_TYPE);
        String publisher = subject.getObjectValue(Predicates.DCTERMS_PUBLISHER);
        String license = subject.getObjectValue(Predicates.DCTERMS_RIGHTS);
        String attribution = subject.getObjectValue(Predicates.DCTERMS_BIBLIOGRAPHIC_CITATION);
        String source = subject.getObjectValue(Predicates.DCTERMS_SOURCE);
        long fileSize = Long.parseLong(subject.getObjectValue(Predicates.CR_BYTE_SIZE));
        Collection<String> uniqueColumns = subject.getObjectValues(Predicates.CR_OBJECTS_UNIQUE_COLUMN);

        String folderUri = StringUtils.substringBeforeLast(fileUri, "/");
        String relativeFilePath = FolderUtil.extractPathInUserHome(fileUri);
        relativeFilePath = StringUtils.replace(relativeFilePath, "%20", " ");

        // Clear graph.
        DAOFactory.get().getDao(HarvestSourceDAO.class).clearGraph(fileUri);

        CsvImportHelper helper =
                new CsvImportHelper(new ArrayList<String>(uniqueColumns), fileUri, fileLabel, fileType, objectsType, publisher,
                        license, attribution, source);

        // Store file as new source, but don't harvest it
        helper.insertFileMetadataAndSource(fileSize, userName);

        // Add metadata about user folder update
        helper.linkFileToFolder(folderUri, userName);

        // Parse and insert triples from file to triplestore
        CSVReader csvReader = null;

        try {
            csvReader = helper.createCSVReader(folderUri, relativeFilePath, userName, true);
            if (csvReader == null) {
                throw new IllegalStateException("No CSV reader successfully created!");
            }
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

            // Finally, make sure that the file has the correct number of harvested statements in its predicates.
            DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatementsTriple(fileUri);

        } catch (Exception e) {
            LOGGER.error("Exception while processing the uploaded file", e);
            warningMessages.add("Exception while reading uploaded file: " + e.getMessage());
        } finally {
            try {
                helper.generateAndStoreTableFileQuery();
            } catch (Exception e2) {
                LOGGER.error("Failed to generate SPARQL query", e2);
            }
            CsvImportHelper.close(csvReader);
        }

        return warningMessages;
    }

}
