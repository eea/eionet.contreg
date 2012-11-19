package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.dto.UserFolderDTO;

/**
 * Reader for user homes.
 *
 * @author kaido
 */
public class UserHomesReader extends ResultSetMixedReader<UserFolderDTO> {

    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {
        if (bindingSet != null && bindingSet.size() > 0) {

            Value folder = bindingSet.getValue("subject");
            Value label = bindingSet.getValue("label");
            Value parentFolder = bindingSet.getValue("parent");
            Value filesCount = bindingSet.getValue("fileCount");
            Value foldersCount = bindingSet.getValue("folderCount");

            UserFolderDTO folderItem = new UserFolderDTO();
            folderItem.setUrl(folder.stringValue());

            folderItem.setLabel(label.stringValue());
            folderItem.setParentFolderUrl(parentFolder.stringValue());
            folderItem.setSubFilesCount(Integer.valueOf(filesCount.stringValue()));
            folderItem.setSubFoldersCount(Integer.valueOf(foldersCount.stringValue()));
            resultList.add(folderItem);
        }
    }

    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        throw new ResultSetReaderException("Not implemented");
    }


}
