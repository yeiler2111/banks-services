package com.nexusbank.account.infrastructure.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Traduce los roles de Keycloak a permisos de Spring Security.
 *
 * <p>Keycloak los pone en el token así:
 * <pre>
 *   "realm_access": { "roles": ["ROLE_CUSTOMER", "offline_access", ...] }
 * </pre>
 * Spring, por defecto, solo lee el claim "scope". Este conversor lee "realm_access.roles"
 * para que funcione hasRole("CUSTOMER") (Spring busca la authority "ROLE_CUSTOMER").
 */
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();
    }
}
