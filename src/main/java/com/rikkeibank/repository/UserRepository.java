package com.rikkeibank.repository;

import com.rikkeibank.model.User;
import com.rikkeibank.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsernameOrEmailOrPhoneNumber(String username, String email, String phoneNumber);

    @Query("select new com.rikkeibank.dto.UserResponse(u.id,u.username,u.email,u.phoneNumber,u.role.name,u.active,u.kyc,u.createdAt) from User u")
    Page<UserResponse> findProjected(Pageable pageable);
}
