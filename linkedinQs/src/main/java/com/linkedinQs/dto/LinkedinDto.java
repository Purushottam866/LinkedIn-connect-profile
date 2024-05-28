package com.linkedinQs.dto;

import org.springframework.stereotype.Component;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Component
@Data
@Entity
public class LinkedinDto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	private String linkedinProfileId;
	private String linkedinProfileFirstName;
	private String linkedinProfileLastName;
	private String linkedinProfileEmail;
	private String linkedinPageId;
	private String LinkedinPageName;
	@Column(length = 1000)
	private String linkedinAccesstoken;
	
}
