package com.system.reservation.online.repository;

import com.system.reservation.online.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail (String name);
    List<User> findByNameContainingIgnoreCase(String name);

    boolean existsByEmail(String email);
}
