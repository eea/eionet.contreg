package eionet.cr.util.export;

import eionet.cr.dto.SubjectDTO;

/**
 *
 * Interface for subject export events.
 *
 * @author Enriko Käsper
 * @author Jaanus Heinlaid
 */
public interface SubjectExportEvent {

    /**
     *
     * @param subject
     * @throws ExportException
     */
    void writeSubjectIntoExporterOutput(SubjectDTO subject) throws ExportException;
}
