package org.keycloak.services.resources.admin;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.common.ClientConnection;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.LDAPCapabilityRepresentation;
import org.keycloak.representations.idm.TestLdapConnectionRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.managers.LDAPServerCapabilitiesManager;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;


public class LdapServerCapabilitiesResource {

    protected final RealmModel realm;

    protected final AdminPermissionEvaluator auth;

    protected final AdminEventBuilder adminEvent;

    protected final ClientConnection clientConnection;

    protected final KeycloakSession session;

    protected final HttpHeaders headers;

    public LdapServerCapabilitiesResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        this.session = session;
        this.auth = auth;
        this.realm = session.getContext().getRealm();
        this.adminEvent = adminEvent;
        this.clientConnection = session.getContext().getConnection();
        this.headers = session.getContext().getRequestHeaders();
    }

    /**
     * Get LDAP supported extensions.
     * @param config LDAP configuration
     * @return
     */
    @POST
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
    public Response ldapServerCapabilities(TestLdapConnectionRepresentation config) {
        auth.realm().requireManageRealm();
        try {
            Set<LDAPCapabilityRepresentation> ldapCapabilities = LDAPServerCapabilitiesManager.queryServerCapabilities(config, session, realm);
            return Response.ok().entity(ldapCapabilities).build();
        } catch (Exception e) {
            return ErrorResponse.error("ldapServerCapabilities error", Response.Status.BAD_REQUEST);
        }
    }

}
