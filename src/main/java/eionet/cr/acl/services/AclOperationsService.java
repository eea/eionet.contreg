package eionet.cr.acl.services;

import eionet.cr.acl.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.acl.errors.AclPropertiesInitializationException;

import java.util.Hashtable;
import java.util.Vector;

public interface AclOperationsService {

    Hashtable<String, Vector<String>> getRefreshedGroupsAndUsersHashTable(boolean init) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException;
}
