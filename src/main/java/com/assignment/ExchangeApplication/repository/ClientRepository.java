package com.assignment.ExchangeApplication.repository;

import com.assignment.ExchangeApplication.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<Client> findAll();

    Client deleteByUsername(String username);
}
