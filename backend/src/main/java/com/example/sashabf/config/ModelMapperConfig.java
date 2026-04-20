package com.example.sashabf.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.sashabf.dto.DocenteDepartamentoDto;
import com.example.sashabf.dto.DocenteDTO;
import com.example.sashabf.entity.Docente;

@Configuration
public class ModelMapperConfig {

    

	@Bean
	public ModelMapper modelMapper() {
	    ModelMapper modelMapper = new ModelMapper();
	    
	    
	    modelMapper.getConfiguration()
	        .setMatchingStrategy(MatchingStrategies.STRICT);
	        
	    return modelMapper;
	}
}