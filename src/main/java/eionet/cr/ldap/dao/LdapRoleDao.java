package eionet.cr.ldap.dao;

import eionet.cr.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;

import java.util.List;

public interface LdapRoleDao {

    /**
     * fetches user ldap roles
     * @param user
     * @return
     * @throws Exception
     */
    public List<LdapRole> findUserRoles(String user) throws LdapDaoException;

    /**
     * fetches users that belong to role roleName
     * @param roleName
     * @return
     * @throws LdapDaoException
     */
    public List<String> findRoleUsers(String roleName) throws LdapDaoException;

    /**
     * fetches all ldap roles
     * @return
     * @throws Exception
     */
    public List<LdapRole> findAllRoles() throws LdapDaoException;
}
