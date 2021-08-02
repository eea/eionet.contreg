package eionet.cr.ldap.services.impl;

import eionet.cr.ldap.dao.LdapRoleDao;
import eionet.cr.ldap.errors.LdapDaoException;
import eionet.cr.ldap.model.LdapRole;
import eionet.cr.ldap.services.LdapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("ldapService")
public class LdapServiceImpl implements LdapService {

    private final LdapRoleDao ldapRoleDao;

    private List<LdapRole> ldapRoles = new ArrayList<>();

    @Autowired
    public LdapServiceImpl(LdapRoleDao ldapRoleDao) {
        this.ldapRoleDao = ldapRoleDao;
    }

    @Override
    public List<LdapRole> getUserLdapRoles(String user) throws LdapDaoException {
        ldapRoles = ldapRoleDao.findUserRoles(user);
        return ldapRoles;
    }
}
