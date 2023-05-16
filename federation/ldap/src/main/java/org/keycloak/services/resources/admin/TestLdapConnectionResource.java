package org.keycloak.services.resources.admin;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.LDAPConstants;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.TestLdapConnectionRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.LDAPServerCapabilitiesManager;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class TestLdapConnectionResource {
    protected final RealmModel realm;

    protected final AdminPermissionEvaluator auth;

    protected final KeycloakSession session;

    public TestLdapConnectionResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
    }

    /**
     * Test LDAP connection
     *
     * @param action
     * @param connectionUrl
     * @param bindDn
     * @param bindCredential
     * @return
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Deprecated
    public Response testLDAPConnection(@FormParam("action") String action, @FormParam("connectionUrl") String connectionUrl,
                                       @FormParam("bindDn") String bindDn, @FormParam("bindCredential") String bindCredential,
                                       @FormParam("useTruststoreSpi") String useTruststoreSpi, @FormParam("connectionTimeout") String connectionTimeout,
                                       @FormParam("componentId") String componentId, @FormParam("startTls") String startTls) {
        auth.realm().requireManageRealm();

        TestLdapConnectionRepresentation config = new TestLdapConnectionRepresentation(action, connectionUrl, bindDn, bindCredential, useTruststoreSpi, connectionTimeout, startTls, LDAPConstants.AUTH_TYPE_SIMPLE);
        config.setComponentId(componentId);
        boolean result = LDAPServerCapabilitiesManager.testLDAP(config, session, realm);
        return result ? Response.noContent().build() : ErrorResponse.error("LDAP test error", Response.Status.BAD_REQUEST);
    }

    /**
     * Test LDAP connection
     * @return
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testLDAPConnection(TestLdapConnectionRepresentation config) {
        boolean result = LDAPServerCapabilitiesManager.testLDAP(config, session, realm);
        return result ? Response.noContent().build() : ErrorResponse.error("LDAP test error", Response.Status.BAD_REQUEST);
    }

}
