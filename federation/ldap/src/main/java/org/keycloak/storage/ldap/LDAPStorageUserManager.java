package org.keycloak.storage.ldap;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.mappers.LDAPTransaction;

/**
 * Track which LDAP users were already enlisted during this transaction
 */
public class LDAPStorageUserManager {

    private final Map<String, ManagedUserEntry> managedUsers = new HashMap<>();
    private final LDAPStorageProvider provider;

    public LDAPStorageUserManager(LDAPStorageProvider provider) {
        this.provider = provider;
    }

    public UserModel getManagedProxiedUser(String userId) {
        ManagedUserEntry entry = managedUsers.get(userId);
        return entry==null ? null : entry.getManagedProxiedUser();
    }

    public LDAPObject getManagedLDAPUser(String userId) {
        ManagedUserEntry entry = managedUsers.get(userId);
        return entry==null ? null : entry.getLdapUser();
    }

    public LDAPTransaction getTransaction(String userId) {
        ManagedUserEntry entry = managedUsers.get(userId);
        if (entry == null) {
            throw new IllegalStateException("Shouldn't happen to not have entry for userId: " + userId);
        }

        return entry.getLdapTransaction();

    }

    public void setManagedProxiedUser(UserModel proxiedUser, LDAPObject ldapObject) {
        String userId = proxiedUser.getId();
        ManagedUserEntry entry = managedUsers.get(userId);
        if (entry != null) {
            throw new IllegalStateException("Don't expect to have entry for user " + userId);
        }

        LDAPTransaction ldapTransaction = new LDAPTransaction(provider, ldapObject);
        ManagedUserEntry newEntry = new ManagedUserEntry(proxiedUser, ldapObject, ldapTransaction);
        managedUsers.put(userId, newEntry);
    }

    public void removeManagedUserEntry(String userId) {
        managedUsers.remove(userId);
    }



    private static class ManagedUserEntry {

        private final UserModel managedProxiedUser;
        private final LDAPObject ldapUser;
        private final LDAPTransaction ldapTransaction;

        public ManagedUserEntry(UserModel managedProxiedUser, LDAPObject ldapUser, LDAPTransaction ldapTransaction) {
            this.managedProxiedUser = managedProxiedUser;
            this.ldapUser = ldapUser;
            this.ldapTransaction = ldapTransaction;
        }

        public UserModel getManagedProxiedUser() {
            return managedProxiedUser;
        }

        public LDAPObject getLdapUser() {
            return ldapUser;
        }

        public LDAPTransaction getLdapTransaction() {
            return ldapTransaction;
        }
    }
}
