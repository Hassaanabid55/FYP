package org.keycloak.storage.ldap;

import org.jboss.logging.Logger;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPIdentityStore;
import org.keycloak.storage.ldap.mappers.LDAPConfigDecorator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class LDAPIdentityStoreRegistry {

    private static final Logger logger = Logger.getLogger(LDAPIdentityStoreRegistry.class);

    private Map<String, LDAPIdentityStoreContext> ldapStores = new ConcurrentHashMap<>();

    public LDAPIdentityStore getLdapStore(KeycloakSession session, ComponentModel ldapModel, Map<ComponentModel, LDAPConfigDecorator> configDecorators) {
        LDAPIdentityStoreContext context = ldapStores.get(ldapModel.getId());

        // Ldap config might have changed for the realm. In this case, we must re-initialize
        MultivaluedHashMap<String, String> configModel = ldapModel.getConfig();
        LDAPConfig ldapConfig = new LDAPConfig(configModel);
        for (Map.Entry<ComponentModel, LDAPConfigDecorator> entry : configDecorators.entrySet()) {
            ComponentModel mapperModel = entry.getKey();
            LDAPConfigDecorator decorator = entry.getValue();

            decorator.updateLDAPConfig(ldapConfig, mapperModel);
        }

        if (context == null || !ldapConfig.equals(context.config)) {
            logLDAPConfig(session, ldapModel, ldapConfig);

            LDAPIdentityStore store = createLdapIdentityStore(session, ldapConfig);
            context = new LDAPIdentityStoreContext(ldapConfig, store);
            ldapStores.put(ldapModel.getId(), context);
        }
        return context.store;
    }

    // Don't log LDAP password
    private void logLDAPConfig(KeycloakSession session, ComponentModel ldapModel, LDAPConfig ldapConfig) {
        logger.infof("Creating new LDAP Store for the LDAP storage provider: '%s', LDAP Configuration: %s", ldapModel.getName(), ldapConfig.toString());

        if (logger.isDebugEnabled()) {
            RealmModel realm = session.realms().getRealm(ldapModel.getParentId());
            realm.getComponentsStream(ldapModel.getId()).forEach(c ->
                    logger.debugf("Mapper for provider: %s, Mapper name: %s, Provider: %s, Mapper configuration: %s",
                            ldapModel.getName(), c.getName(), c.getProviderId(), c.getConfig().toString()));
        }
    }

    /**
     * Create LDAPIdentityStore to be cached in the local registry
     */
    public static LDAPIdentityStore createLdapIdentityStore(KeycloakSession session, LDAPConfig cfg) {
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.authentication", cfg.getConnectionPoolingAuthentication(), "none simple");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.initsize", cfg.getConnectionPoolingInitSize(), "1");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.maxsize", cfg.getConnectionPoolingMaxSize(), "1000");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.prefsize", cfg.getConnectionPoolingPrefSize(), "5");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.timeout", cfg.getConnectionPoolingTimeout(), "300000");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.protocol", cfg.getConnectionPoolingProtocol(), "plain ssl");
        checkSystemProperty("com.sun.jndi.ldap.connect.pool.debug", cfg.getConnectionPoolingDebug(), "off");

        return new LDAPIdentityStore(session, cfg);
    }

    private static void checkSystemProperty(String name, String cfgValue, String defaultValue) {
        String value = System.getProperty(name);
        if(cfgValue != null) {
            value = cfgValue;
        }
        if(value == null) {
            value = defaultValue;
        }
        System.setProperty(name, value);
    }


    private static class LDAPIdentityStoreContext {

        private LDAPIdentityStoreContext(LDAPConfig config, LDAPIdentityStore store) {
            this.config = config;
            this.store = store;
        }

        private LDAPConfig config;
        private LDAPIdentityStore store;
    }
}
