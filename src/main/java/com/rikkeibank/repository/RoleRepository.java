package com.rikkeibank.repository;

import com.rikkeibank.domain.Role;
import com.rikkeibank.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
