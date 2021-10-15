package eionet.cr.web;

import eionet.cr.errors.UserExistsException;
import eionet.cr.errors.XmlMalformedException;

public interface HandleGroupService {

    void addUserToGroup(String username, String groupName) throws UserExistsException, XmlMalformedException;

    void removeUserFromGroup(String userName, String groupName) throws XmlMalformedException;
}
