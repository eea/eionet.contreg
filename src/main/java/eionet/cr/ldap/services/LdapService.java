package eionet.cr.ldap.services;

import eionet.cr.ldap.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;

import java.util.List;

public interface LdapService {

    /**
     * fetches user ldap roles
     * @return
     */
    List<LdapRole> getUserLdapRoles(String user) throws LdapDaoException;
}
