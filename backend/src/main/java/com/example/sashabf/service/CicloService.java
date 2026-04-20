package com.example.sashabf.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sashabf.entity.Ciclo;
import com.example.sashabf.repository.CicloRepository;
@Service
public class CicloService {

	@Autowired
	private CicloRepository cicloRepository;
	
}
