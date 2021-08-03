package eionet.cr.acl.services.impl;

import eionet.cr.acl.services.AclService1;
import org.springframework.stereotype.Service;

@Service
public class AclService1Impl implements AclService1 {

    @Override
    public boolean hasPermission() {
        return false;
    }
}
