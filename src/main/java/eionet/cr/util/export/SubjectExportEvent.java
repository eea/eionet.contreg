package eionet.cr.util.export;

import eionet.cr.dto.SubjectDTO;

public interface SubjectExportEvent {

    public void writeSubjectIntoExporterOutput(SubjectDTO subject) throws ExportException;

}
