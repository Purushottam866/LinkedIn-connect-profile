package com.linkedinQs.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.linkedinQs.dto.LinkedinDto;
import com.linkedinQs.repository.LinkedinRepository;

@Component
public class LinkedinDao {

	@Autowired
	LinkedinRepository linkedinRepository;
	
	public void save(LinkedinDto linkedinDto)
	{
		linkedinRepository.save(linkedinDto);
	}
	
	public LinkedinDto findById(int id) {
		return linkedinRepository.findById(id).orElseThrow(null);
	}
	
}
