package com.example.the_labot_backend.admins.repository;

import com.example.the_labot_backend.admins.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}