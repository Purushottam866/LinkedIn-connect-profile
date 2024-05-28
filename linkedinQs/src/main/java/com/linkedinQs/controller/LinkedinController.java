package com.linkedinQs.controller;



import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import com.linkedinQs.helper.ResponseStructure;
import com.linkedinQs.service.LinkedinService;


@RestController
@RequestMapping("/quantumshare")
public class LinkedinController {

	@Autowired
    LinkedinService linkedinService;
	
	@Autowired
	com.linkedinQs.helper.ResponseStructure<String> responseStructure;

    @GetMapping("/connect/linkedIn")
    public RedirectView login() {
        String authorizationUrl = linkedinService.generateAuthorizationUrl();
        return new RedirectView(authorizationUrl);
    }

    @GetMapping("/callback/success")
    public ResponseEntity<String> callbackEndpoint(@RequestParam("code") String code) throws IOException {
        return linkedinService.getUserInfoWithToken(code);
    }
    
    //TEXT AND MEDIA UPLOAD TO PROFILE
    @PostMapping("/postToProfile") 
    public ResponseEntity<ResponseStructure<String>> createPostTOProfile(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {

        ResponseStructure<String> response;

        if (file != null && !file.isEmpty() && caption != null && !caption.isEmpty()) {
            // Both file and caption are present
            response = linkedinService.uploadImageToLinkedIn(file, caption);
        } else if (caption != null && !caption.isEmpty()) {
            // Only caption is present
            response = linkedinService.createPostProfile(caption);
        } else if (file != null && !file.isEmpty()) {
            // Only file is present
        	 response = linkedinService.uploadImageToLinkedIn(file, "");
        } else {
            // Neither file nor caption are present
            response = new ResponseStructure<>();
            response.setStatus("Failure");
            response.setMessage("Either file or caption must be provided.");
            response.setCode(HttpStatus.BAD_REQUEST.value());
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }
    
    
 // TEXT AND MEDIA UPLOAD TO PAGE/ORGANIZATION
    @PostMapping("/postToPage")
    public ResponseEntity<ResponseStructure<String>> createPost(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption) {

        ResponseStructure<String> response;

        if (file != null && !file.isEmpty() && caption != null && !caption.isEmpty()) {
            // Both file and caption are present
            response = linkedinService.uploadImageToLinkedInPage(file, caption);
        } else if (caption != null && !caption.isEmpty()) {
            // Only caption is present
            response = linkedinService.createPostPage(caption);
        } else if (file != null && !file.isEmpty()) {
            // Only file is present
        	 response = linkedinService.uploadImageToLinkedInPage(file, "");
        } else {
            // Neither file nor caption are present
            response = new ResponseStructure<>();
            response.setStatus("Failure");
            response.setMessage("Either file or caption must be provided.");
            response.setCode(HttpStatus.BAD_REQUEST.value());
        }

        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getCode()));
    }
	
}
