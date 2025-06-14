package com.assignment.ExchangeApplication.model;

import com.assignment.ExchangeApplication.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Client implements UserDetails{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    @JsonIgnore
    private String password;

    private String name;

    @Column(unique = true)
    private String username;


    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Account> accounts;

    @CreationTimestamp
    private Date createdAt;

    private List<GrantedAuthority> authorities;

    @JsonIgnore
    private UserRole role;

    public Client(UUID id, String email, String password, String name, String username, Date createdAt, List<GrantedAuthority> authorities, UserRole role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.username = username;
        this.createdAt = createdAt;
        this.authorities = authorities;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

