package org.keycloak.storage.ldap;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.keycloak.models.UserModel;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ReadOnlyException;

/**
 * Will be good to get rid of this class and use ReadOnlyUserModelDelegate, but it can't be done now due the backwards compatibility.
 * And because we don't have much time; our Mid-Defense is here!
 */
public class ReadonlyLDAPUserModelDelegate extends UserModelDelegate {

    public ReadonlyLDAPUserModelDelegate(UserModel delegate) {
        super(delegate);
    }

    @Override
    public void setUsername(String username) {
        if (!Objects.equals(getUsername(), username)) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }

    @Override
    public void setLastName(String lastName) {
        if (!Objects.equals(getLastName(), lastName)) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }

    @Override
    public void setFirstName(String first) {
        if (!Objects.equals(getFirstName(), first)) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }

    @Override
    public void setEmail(String email) {
        if (!Objects.equals(getEmail(), email)) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        if (!Objects.equals(getAttributeStream(name).collect(Collectors.toList()), Collections.singletonList(value))) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        if (!Objects.equals(getAttributeStream(name).collect(Collectors.toList()), values)) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (getAttributeStream(name).count() > 0) {
            throw new ReadOnlyException("Federated storage is not writable");
        }
    }
}
