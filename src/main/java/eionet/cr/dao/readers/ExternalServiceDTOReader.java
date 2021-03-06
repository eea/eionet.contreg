package eionet.cr.dao.readers;

import eionet.cr.dto.ExternalServiceDTO;
import eionet.cr.dto.enums.ExternalServiceType;
import eionet.cr.util.sql.SQLResultSetBaseReader;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Reader for converting a resultset to ExternalServiceDTO objects.
 */
public class ExternalServiceDTOReader  extends SQLResultSetBaseReader<ExternalServiceDTO> {

    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        ExternalServiceDTO externalServiceDTO = new ExternalServiceDTO();
        externalServiceDTO.setServiceId(new Integer(rs.getInt("SERVICE_ID")));
        externalServiceDTO.setServiceUrl(rs.getString("SERVICE_URL"));
        externalServiceDTO.setSecureToken(rs.getString("SECURE_TOKEN"));
        externalServiceDTO.setServiceType(ExternalServiceType.valueOf(rs.getString("SERVICE_TYPE")));
        externalServiceDTO.setUserId(rs.getString("USER_NAME"));

        resultList.add(externalServiceDTO);
    }
}
