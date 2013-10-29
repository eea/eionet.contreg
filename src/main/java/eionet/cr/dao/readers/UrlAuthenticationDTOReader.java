package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;

import eionet.cr.dto.UrlAuthenticationDTO;
import eionet.cr.util.sql.SQLResultSetBaseReader;

public class UrlAuthenticationDTOReader extends SQLResultSetBaseReader<UrlAuthenticationDTO> {

    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        UrlAuthenticationDTO result = new UrlAuthenticationDTO();

        result.setId(new Integer(rs.getInt("authurl_id")));
        result.setPassword(rs.getString("url_password"));
        result.setUrlBeginning(rs.getString("url_namestart"));
        result.setUsername(rs.getString("url_username"));

        resultList.add(result);
    }



}
