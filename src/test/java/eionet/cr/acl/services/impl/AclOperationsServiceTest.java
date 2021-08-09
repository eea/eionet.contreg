package eionet.cr.acl.services.impl;

import eionet.acl.AccessController;
import eionet.acl.AclProperties;
import eionet.cr.errors.AclAccessControllerInitializationException;
import eionet.cr.errors.AclLibraryAccessControllerModifiedException;
import eionet.cr.errors.AclPropertiesInitializationException;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AclOperationsServiceTest {

    @Mock
    ConfigurationPropertyResolver configurationPropertyResolver;

    AclOperationsServiceImpl aclOperationsService;

    @Before
    public void setUp() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        MockitoAnnotations.openMocks(this);
        this.aclOperationsService = new AclOperationsServiceImpl(configurationPropertyResolver);
    }


    @Test(expected = AclPropertiesInitializationException.class)
    public void testGetAclPropertiesThrowsAclPropertiesInitializationException() throws UnresolvedPropertyException, CircularReferenceException, AclPropertiesInitializationException {
        when(this.configurationPropertyResolver.resolveValue(any(String.class))).thenThrow(UnresolvedPropertyException.class);
        this.aclOperationsService.getAclProperties();
    }

    @Test
    public void testSucessGettingAclProperties() throws AclPropertiesInitializationException {
        final AclProperties aclProperties = mock(AclProperties.class);
        AclOperationsServiceImpl aclOperationsServiceImpl = new AclOperationsServiceImpl(this.configurationPropertyResolver) {
            @Override
            protected AclProperties getAclProperties() {
                return aclProperties;
            }
        };
        assertThat(aclOperationsServiceImpl.getAclProperties(), equalTo(aclProperties));
    }


    @Test(expected = AclLibraryAccessControllerModifiedException.class)
    public void testGetGroupsAndUsersHashTableThrowsAclLibraryAccessControllerModifiedException() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        final AclProperties aclProperties = mock(AclProperties.class);
        AclOperationsServiceImpl aclOperationsServiceImpl = new AclOperationsServiceImpl(this.configurationPropertyResolver) {
            @Override
            protected AclProperties getAclProperties() {
                return aclProperties;
            }

            @Override
            protected AccessController getAclLibraryAccessControllerInstance(AclProperties aclProperties) throws AclAccessControllerInitializationException {
                throw new AclAccessControllerInitializationException();
            }
        };
        aclOperationsServiceImpl.getRefreshedGroupsAndUsersHashTable(false);
    }


    @Test(expected = AclPropertiesInitializationException.class)
    public void testGetGroupsAndUsersHashTableThrowsAclPropertiesInitializationException() throws AclLibraryAccessControllerModifiedException, AclPropertiesInitializationException {
        AclOperationsServiceImpl aclOperationsServiceImpl = new AclOperationsServiceImpl(this.configurationPropertyResolver) {
            @Override
            protected AclProperties getAclProperties() throws AclPropertiesInitializationException {
                throw new AclPropertiesInitializationException();
            }

            @Override
            protected AccessController getAclLibraryAccessControllerInstance(AclProperties aclProperties) throws AclAccessControllerInitializationException {
                throw new AclAccessControllerInitializationException();
            }
        };
        aclOperationsServiceImpl.getRefreshedGroupsAndUsersHashTable(false);
    }
}
