package com.guarani.pos.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.guarani.pos.auth.model.AuthSecurityEvent;

public interface AuthSecurityEventRepository extends JpaRepository<AuthSecurityEvent, Long> {
}
