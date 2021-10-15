package eionet.cr.ldap.services;

import eionet.cr.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;

import java.util.List;

public interface LdapService {

    /**
     * fetches user ldap roles
     * @return
     */
    List<LdapRole> getUserLdapRoles(String user) throws LdapDaoException;

    /**
     * fetches users that belong to role roleName
     * @param roleName
     * @return
     * @throws LdapDaoException
     */
    List<String> getRoleUsers(String roleName) throws LdapDaoException;

    /**
     * fetches all ldap roles
     * @return
     */
    List<LdapRole> getAllLdapRoles() throws LdapDaoException;
}
