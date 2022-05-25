package com.optimagrowth.license.service.client;

import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.repository.OrganizationRedisRepository;
import com.optimagrowth.license.utils.UserContextHolder;

import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrganizationRestTemplateClient {
    // @Autowired
    // RestTemplate restTemplate;
    @Autowired
    OrganizationRedisRepository redisRepository;

    @Autowired
    KeycloakRestTemplate restTemplate;

    public Organization getOrganization(String organizationId) {
        log.debug("In Licensing Service.getOrganization: {}", UserContextHolder.getContext().getCorrelationId());
        Organization organization = checkRedisCache(organizationId);
        if (organization != null) {
            log.debug("I have successfully retrieved an organization {} from the redis cache: {}", organizationId,
                    organization);
            return organization;
        }

        log.debug("Unable to locate organization from the redis cache: {}.", organizationId);
        ResponseEntity<Organization> restExchange = restTemplate.exchange(
                "http://organization-service/v1/organization/{organizationId}",
                HttpMethod.GET,
                null, Organization.class, organizationId);

        organization = restExchange.getBody();
        if (organization != null) {
            cacheOrganizationObject(organization);
        }
        return restExchange.getBody();
    }

    private Organization checkRedisCache(String organizationId) {
        try {
            return redisRepository.findById(organizationId)
                    .orElse(null);
        } catch (Exception ex) {
            log.error(
                    "Error encountered while trying to retrieve organization {} check Redis Cache. Exception {}.",
                    organizationId,
                    ex);
            return null;
        }
    }

    private void cacheOrganizationObject(Organization organization) {
        try {
            redisRepository.save(organization);
        } catch (Exception ex) {
            log.error(
                    "Unable to cache organization: {} in Redis Cache. Exception: {}.",
                    organization.getId(),
                    ex);
        }
    }
}
