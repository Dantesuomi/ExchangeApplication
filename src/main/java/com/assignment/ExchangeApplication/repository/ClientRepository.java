package com.assignment.ExchangeApplication.repository;

import com.assignment.ExchangeApplication.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<Client> findAll();

    Client deleteByUsername(String username);
}
