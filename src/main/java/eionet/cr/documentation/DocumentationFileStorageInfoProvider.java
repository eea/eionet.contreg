package eionet.cr.documentation;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.doc.io.FileStorageInfoProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * File storage provider.
 */
@Component
@DependsOn("springApplicationContext")
public final class DocumentationFileStorageInfoProvider implements FileStorageInfoProvider {
    
    private static final String DOC_FOLDER_PROPERTY_NAME = "doc.files.folder";
    
    private final String fileStoragePath;
    
    public DocumentationFileStorageInfoProvider() throws IOException {
        this.fileStoragePath = StringUtils.trim(GeneralConfig.getProperty(DOC_FOLDER_PROPERTY_NAME));
        
        if (StringUtils.isBlank(fileStoragePath)) {
            String msg = String.format("Property '%s' not found", DOC_FOLDER_PROPERTY_NAME);
            throw new CRRuntimeException(msg);
        }
    }
    
    @Override
    public String getFileStoragePath() {
        return this.fileStoragePath;
    }
    
}
