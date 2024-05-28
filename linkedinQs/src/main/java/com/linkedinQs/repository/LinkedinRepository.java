package com.linkedinQs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.linkedinQs.dto.LinkedinDto;

@Repository
public interface LinkedinRepository extends JpaRepository<LinkedinDto, Integer> {

	
}
