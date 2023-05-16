package org.keycloak.storage.ldap;

import java.util.List;
import java.util.function.Function;

import org.keycloak.models.LDAPConstants;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.ReadOnlyUserModelDelegate;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ldap.mappers.LDAPTransaction;

/**
 * User model delegate, which tracks what attributes were written to LDAP in this transaction. For those attributes, it will skip
 * calling delegate for doing any additional updates.
 *
 * It may be typically used together with Read-Only delegate. The result is that read-only exception will be thrown when attempt
 * to update any user attribute, which is NOT mapped to LDAP.
 */
public class LDAPWritesOnlyUserModelDelegate extends UserModelDelegate {

    private final LDAPStorageProvider provider;

    public LDAPWritesOnlyUserModelDelegate(UserModel delegate, LDAPStorageProvider provider) {
        super(delegate);
        this.provider = provider;
    }

    @Override
    public void setUsername(String username) {
        if (!isAttributeUpdatedInLDAP(UserModel.USERNAME)) {
            super.setUsername(username);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!isAttributeUpdatedInLDAP(LDAPConstants.ENABLED)) {
            super.setEnabled(enabled);
        }
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (!isAttributeUpdatedInLDAP(name)) {
            super.setSingleAttribute(name, value);
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (!isAttributeUpdatedInLDAP(name)) {
            super.setAttribute(name, values);
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (!isAttributeUpdatedInLDAP(name)) {
            super.removeAttribute(name);
        }
    }

    @Override
    public void addRequiredAction(String action) {
        if (!isRequiredActionUpdatedInLDAP(action)) {
            super.addRequiredAction(action);
        }
    }

    @Override
    public void removeRequiredAction(String action) {
        if (!isRequiredActionUpdatedInLDAP(action)) {
            super.removeRequiredAction(action);
        }
    }

    @Override
    public void addRequiredAction(RequiredAction action) {
        if (!isRequiredActionUpdatedInLDAP(action.toString())) {
            super.addRequiredAction(action);
        }
    }

    @Override
    public void removeRequiredAction(RequiredAction action) {
        if (!isRequiredActionUpdatedInLDAP(action.toString())) {
            super.removeRequiredAction(action);
        }
    }

    @Override
    public void setFirstName(String firstName) {
        if (!isAttributeUpdatedInLDAP(UserModel.FIRST_NAME)) {
            super.setFirstName(firstName);
        }
    }

    @Override
    public void setLastName(String lastName) {
        if (!isAttributeUpdatedInLDAP(UserModel.LAST_NAME)) {
            super.setLastName(lastName);
        }
    }

    @Override
    public void setEmail(String email) {
        if (!isAttributeUpdatedInLDAP(UserModel.EMAIL)) {
            super.setEmail(email);
        }
    }

    @Override
    public void setEmailVerified(boolean verified) {
        if (!isAttributeUpdatedInLDAP("emailVerified")) {
            super.setEmailVerified(verified);
        }
    }

    // Checks if attribute was updated in LDAP in this transaction
    protected boolean isAttributeUpdatedInLDAP(String attributeName) {
        LDAPTransaction transaction = provider.getUserManager().getTransaction(getId());
        if (transaction == null) return false;
        return transaction.isAttributeUpdated(attributeName);
    }

    // Checks if requiredAction was updated in LDAP in this transaction
    protected boolean isRequiredActionUpdatedInLDAP(String requiredActionName) {
        LDAPTransaction transaction = provider.getUserManager().getTransaction(getId());
        if (transaction == null) return false;
        return transaction.isRequiredActionUpdated(requiredActionName);
    }



}
