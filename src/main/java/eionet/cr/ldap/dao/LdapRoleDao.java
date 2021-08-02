package eionet.cr.ldap.dao;

import eionet.cr.ldap.errors.LdapDaoException;
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
}
