package com.example.sashabf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Rol;

@Repository
public interface RolRepository extends JpaRepository <Rol,Long> {

	

}
