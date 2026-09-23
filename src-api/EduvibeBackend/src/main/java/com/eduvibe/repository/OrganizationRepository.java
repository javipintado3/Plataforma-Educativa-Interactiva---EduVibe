package com.eduvibe.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eduvibe.model.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByAllowedDomain(String allowedDomain);
}
