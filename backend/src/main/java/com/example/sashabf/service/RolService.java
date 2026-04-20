package com.example.sashabf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sashabf.repository.RolRepository;

@Service

public class RolService {
 @Autowired
 private RolRepository rolRepository;
 
}
