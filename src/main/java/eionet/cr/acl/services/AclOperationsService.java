package eionet.cr.acl.services;

import eionet.cr.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.errors.AclPropertiesInitializationException;

import java.util.Hashtable;
import java.util.Vector;

public interface AclOperationsService {

    Hashtable<String, Vector<String>> getRefreshedGroupsAndUsersHashTable(boolean init) throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException;
}
