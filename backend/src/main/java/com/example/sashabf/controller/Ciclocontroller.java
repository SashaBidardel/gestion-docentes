package com.example.sashabf.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.sashabf.service.CicloService;

public class Ciclocontroller {
    
	private final CicloService cicloService;

	public Ciclocontroller(CicloService cicloService) {
		super();
		this.cicloService = cicloService;
	}
	
}
