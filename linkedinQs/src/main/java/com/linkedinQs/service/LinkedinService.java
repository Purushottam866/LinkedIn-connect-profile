package com.linkedinQs.service;



import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkedinQs.dao.LinkedinDao;
import com.linkedinQs.dto.LinkedinDto;
import com.linkedinQs.helper.ResponseStructure;


@Service
public class LinkedinService {

	@Value("${linkedin.clientId}")
    private String clientId;

    @Value("${linkedin.clientSecret}")
    private String clientSecret;

    @Value("${linkedin.redirectUri}")
    private String redirectUri;

    @Value("${linkedin.scope}")
    private String scope;
    
    @Value("${linkedin.access.token}")
    private String accessToken;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpHeaders httpHeaders;
    
    @Autowired
    LinkedinDto dto;
    
    @Autowired
    LinkedinDao dao;
    
    @Autowired
    com.linkedinQs.helper.ResponseStructure<String> response;

    public String generateAuthorizationUrl() {
        return "https://www.linkedin.com/oauth/v2/authorization" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scope;
    }

    public String exchangeAuthorizationCodeForAccessToken(String code) throws IOException {
        String accessTokenUrl = "https://www.linkedin.com/oauth/v2/accessToken";
        String params = "grant_type=authorization_code" +
                "&code=" + code + 
                "&redirect_uri=" + redirectUri +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret;

        URL url = new URL(accessTokenUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(params);
        }

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String response = parseResponse(connection);
            return parseAccessToken(response);
        } else {
            return null;
        }
    }

    public String getUserInfo(String accessToken) {
        String userInfoUrl = "https://api.linkedin.com/v2/userinfo";

        httpHeaders.setBearerAuth(accessToken);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode userInfo = objectMapper.readTree(response.getBody());
                String sub = userInfo.get("sub").asText();
                String name = userInfo.get("name").asText();
                String email = userInfo.get("email").asText();
                return "Sub: " + sub + ", Name: " + name + ", Email: " + email;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                System.err.println("Unauthorized error: " + e.getMessage());
            } else {
                System.err.println("Error: " + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    } 

    private String parseResponse(HttpURLConnection connection) throws IOException {
        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    private String parseAccessToken(String response) throws IOException {
        return new ObjectMapper().readTree(response).get("access_token").asText();
    }

//    public ResponseEntity<String> getUserInfoWithToken(String code) {
//        try {
//            String accessToken = exchangeAuthorizationCodeForAccessToken(code);
//            if (accessToken != null) {
//                System.out.println("Accesstoken = " + accessToken);
//              //  ResponseEntity<String> organizationAclsResponse = getOrganizationInfo(accessToken);
//                String userInfo = getUserInfo(accessToken);
//               
//                if (userInfo != null) {
//                    Pattern pattern = Pattern.compile("Sub: (.+), Name: (.+), Email: (.+)");
//                    Matcher matcher = pattern.matcher(userInfo);
//
//                    if (matcher.find()) {
//                        String sub = matcher.group(1);
//                        String name = matcher.group(2);
//                        String email = matcher.group(3);
//
//                        // Now you have sub, name, and email stored in individual variables
//                        System.out.println("Sub: " + sub);
//                        System.out.println("Name: " + name);
//                        System.out.println("Email: " + email);
//
//                        // Saving data
    					  
//                        linkedinDto.setLinkedinprofileid(sub);
//                        linkedinDto.setLinkedinprofilename(name);
//                        linkedinDto.setLinkedinprofileemail(email);
//                        linkedinDto.setLinkedinprofileaccesstoken(accessToken);
//                        linkedinDao.save(linkedinDto);
//
//                        // Constructing the response entity
//                        String responseBody = "AccessToken: " + accessToken + "</br>" +
//                                "Sub: " + sub + ", Name: " + name + ", Email: " + email;
//                        HttpHeaders responseHeaders = new HttpHeaders();
//                        responseHeaders.setContentType(MediaType.TEXT_HTML);
//                        return new ResponseEntity<>(responseBody, responseHeaders, HttpStatus.OK);
//                    } else {
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to extract user information from the response.");
//                    }
//                } else {
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve user information.");
//                }
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to exchange authorization code for access token.");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request: " + e.getMessage());
//        }
//    }
    
    public ResponseEntity<String> getUserInfoWithToken(String code) throws IOException {
      
            String accessToken = exchangeAuthorizationCodeForAccessToken(code);
            //ResponseEntity<String> organizationResponse = getMemberOrganization(accessToken);
           ResponseEntity<String> organizationAclsResponse = getOrganizationInfo(accessToken);
			return organizationAclsResponse;
           // return organizationResponse;
        }
    
    public ResponseEntity<String> getOrganizationInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.linkedin.com/v2/me",
                HttpMethod.GET,
                entity,
                String.class);

        String responseBody = response.getBody();

        // Parsing and extracting desired fields from the response
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String localizedFirstName = rootNode.path("localizedFirstName").asText();
            String localizedLastName = rootNode.path("localizedLastName").asText();
            String id = rootNode.path("id").asText();

            // Constructing the custom response JSON
            String customResponse = "{ \"profile_sub\": \"" + id + "\", \"FirstName\": \"" + localizedFirstName + "\", \"LastName\": \"" + localizedLastName + "\", \"access_token\": \"" + accessToken + "\" }";

            LinkedinDto dto = new LinkedinDto();
            dto.setLinkedinProfileId(id);
            dto.setLinkedinProfileFirstName(localizedFirstName);
            dto.setLinkedinProfileLastName(localizedLastName);
            dto.setLinkedinAccesstoken(accessToken);
            
            // Logging the object to check state before saving
            System.out.println("Saving new LinkedinDto: " + dto);
            
            dao.save(dto);  
            
            // Setting custom response headers
            HttpHeaders customHeaders = new HttpHeaders();
            customHeaders.setContentType(MediaType.APPLICATION_JSON);

            return new ResponseEntity<>(customResponse, customHeaders, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request: " + e.getMessage());
        }
    }
    
//    public ResponseEntity<String> getMemberOrganization(String accessToken) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        headers.set("X-Restli-Protocol-Version", "2.0.0");
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.exchange(
//                "https://api.linkedin.com/v2/organizationAcls?q=roleAssignee",
//                HttpMethod.GET,
//                entity,
//                String.class);
//
//        String responseBody = response.getBody();
//
//        // Parsing and extracting desired fields from the response
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            JsonNode rootNode = objectMapper.readTree(responseBody);
//
//            JsonNode elements = rootNode.path("elements");
//            if (elements.isArray() && elements.size() > 0) {
//                JsonNode firstElement = elements.get(0);
//                String profileId = firstElement.path("roleAssignee").asText();
//                String pageId = firstElement.path("organization").asText();
//
//                // Constructing the custom response JSON
//                String customResponse = "{ \"accessToken\": \"" + accessToken + "\", \"profileId\": \"" + profileId + "\", \"pageId\": \"" + pageId + "\" }";
//
//                // Setting custom response headers
//                HttpHeaders customHeaders = new HttpHeaders();
//                customHeaders.setContentType(MediaType.APPLICATION_JSON);
//
//                return new ResponseEntity<>(customResponse, customHeaders, HttpStatus.OK);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No organization ACLs found.");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request: " + e.getMessage());
//        }
//    }
  //r_organization_followers,r_organization_social,rw_organization_admin,r_organization_social_feed,w_member_social,w_organization_social,r_basicprofile,w_organization_social_feed,w_member_social_feed

    
    
    
 // TEXT POSTING TO LINKEDIN PROFILE        urn:li:person:cHTCMRpubB       urn:li:organization:103081264
    public ResponseStructure<String> createPostProfile(String caption) {
 	    ResponseStructure<String> response = new ResponseStructure<>();
 	    try {
 	    	
 	    	
 	    	
 	        System.out.println("Caption: " + caption);
 	        String url = "https://api.linkedin.com/v2/ugcPosts";
 	        String requestBody = "{\"author\":\"urn:li:person:yyjwRekl0f\",\"lifecycleState\":\"PUBLISHED\",\"specificContent\":{\"com.linkedin.ugc.ShareContent\":{\"shareCommentary\":{\"text\":\"" + caption + "\"},\"shareMediaCategory\":\"NONE\"}},\"visibility\":{\"com.linkedin.ugc.MemberNetworkVisibility\":\"PUBLIC\"}}";

 	        HttpHeaders headers = new HttpHeaders();
 	        headers.set("Authorization", "Bearer " + accessToken);
 	        headers.set("Content-Type", "application/json");
 	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

 	        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
 	        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
 	            response.setStatus("Success");
 	            response.setMessage("Post created successfully");
 	            response.setCode(HttpStatus.CREATED.value());
 	            response.setData(responseEntity.getBody());
 	            System.out.println("Response Body: " + responseEntity.getBody());
 	        } else {
 	            response.setStatus("Failure");
 	            response.setMessage("Failed to create post");
 	            response.setCode(responseEntity.getStatusCode().value());
 	            response.setData(responseEntity.getBody());
 	            System.out.println("Error Response: " + responseEntity.getBody());
 	        }
 	    } catch (HttpClientErrorException e) {
 	        response.setStatus("Failure");
 	        response.setMessage("HTTP Client Error: " + e.getStatusCode());
 	        response.setCode(e.getStatusCode().value());
 	        System.out.println("HttpClientErrorException: " + e.getMessage());
 	        e.printStackTrace();
 	    } catch (HttpServerErrorException e) {
 	        response.setStatus("Failure");
 	        response.setMessage("HTTP Server Error: " + e.getStatusCode());
 	        response.setCode(e.getStatusCode().value());
 	        System.out.println("HttpServerErrorException: " + e.getMessage());
 	        e.printStackTrace();
 	    } catch (Exception e) {
 	        response.setStatus("Failure");
 	        response.setMessage("Internal Server Error: " + e.getMessage());
 	        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 	        System.out.println("Exception: " + e.getMessage());
 	        e.printStackTrace();
 	    }
 	    return response;
 	}
    
    
 // SHARE IMAGE/VIDEO AND TEXT TO LINKEDIN PROFILE
    public ResponseStructure<String> uploadImageToLinkedIn(MultipartFile file, String caption) {
 	   
 	    try {
 	    	
 	    	System.out.println("controller is here 1 " + caption + " " + file);
 	    	
 	        String recipeType = determineRecipeType(file);
 	        String mediaType = determineMediaType(file);
 	        JsonNode uploadResponse = registerUpload(recipeType);
 	        String uploadUrl = uploadResponse.get("value").get("uploadMechanism").get("com.linkedin.digitalmedia.uploading.MediaUploadHttpRequest").get("uploadUrl").asText();
 	        String mediaAsset = uploadResponse.get("value").get("asset").asText();
 	        uploadImage(uploadUrl, file);
 	        ResponseStructure<String> postResponse = createLinkedInPost(mediaAsset, caption, mediaType);

 	        // Set the response based on the postResponse
 	        handlePostResponse(response, postResponse);

 	    } catch (HttpClientErrorException.TooManyRequests e) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Too Many Requests - " + e.getMessage());
 	        response.setCode(HttpStatus.TOO_MANY_REQUESTS.value());
 	        response.setData(null);
 	    } catch (HttpClientErrorException e) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
 	        response.setCode(e.getStatusCode().value());
 	        response.setData(null);
 	    } catch (IOException e) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to upload media to LinkedIn: " + e.getMessage());
 	        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 	        response.setData(null);
 	    }
 	    return response;
 	}

 	private void handlePostResponse(ResponseStructure<String> response, ResponseStructure<String> postResponse) {
 	    if (postResponse.getCode() == 201) {
 	        response.setStatus(postResponse.getStatus());
 	        response.setMessage(postResponse.getMessage());
 	        response.setCode(postResponse.getCode());
 	        response.setData(postResponse.getData());
 	    } else if (postResponse.getCode() == 400) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Caption is invalid");
 	        response.setCode(400);
 	        response.setData(null);
 	    } else if (postResponse.getCode() == 401) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Unauthorized access");
 	        response.setCode(401);
 	        response.setData(null);
 	    } else if (postResponse.getCode() == 422) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Media asset error");
 	        response.setCode(422);
 	        response.setData(null);
 	    } else if (postResponse.getCode() == 429) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Too Many Requests");
 	        response.setCode(429);
 	        response.setData(null);
 	    } else if (postResponse.getCode() == 500) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Internal server error");
 	        response.setCode(500);
 	        response.setData(null);
 	    } else if (postResponse.getCode() == 503) {
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Network issues");
 	        response.setCode(503);
 	        response.setData(null);
 	    } else {
 	        // Handle other failure scenarios
 	        response.setStatus("Failure");
 	        response.setMessage("Failed to create LinkedIn post: Unexpected error occurred");
 	        response.setCode(postResponse.getCode());
 	        response.setData(null);
 	    }
 	}

    private String determineRecipeType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image") ? "urn:li:digitalmediaRecipe:feedshare-image" : "urn:li:digitalmediaRecipe:feedshare-video";
    }

    private String determineMediaType(MultipartFile file) {
        return file.getContentType() != null && file.getContentType().startsWith("image") ? "image" : "video";
    }

    private JsonNode registerUpload(String recipeType) throws IOException {
 	   
 	   System.out.println("controller is here 2 " + recipeType);
 	   
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);								// urn:li:person:cHTCMRpubB

        String requestBody = "{\"registerUploadRequest\": {\"recipes\": [\"" + recipeType + "\"],\"owner\": \"urn:li:person:yyjwRekl0f\",\"serviceRelationships\": [{\"relationshipType\": \"OWNER\",\"identifier\": \"urn:li:userGeneratedContent\"}]}}";

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
                "https://api.linkedin.com/v2/assets?action=registerUpload",
                HttpMethod.POST,
                requestEntity,
                JsonNode.class
        );

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new RuntimeException("Failed to register upload: " + responseEntity.getStatusCode());
        }
    }

    private ResponseStructure<String> uploadImage(String uploadUrl, MultipartFile file) {
 	  
 	    try {
 	    	
 	    	System.out.println("controller is here 3 " + uploadUrl + " " + file);
 	    	
 	        HttpHeaders headers = new HttpHeaders();
 	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
 	        headers.set("Authorization", "Bearer " + accessToken);

 	        byte[] fileContent;
 	        try {
 	            fileContent = file.getBytes();
 	        } catch (IOException e) {
 	            response.setStatus("Failure");
 	            response.setMessage("Failed to read image file");
 	            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 	            return response;
 	        }

 	        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContent, headers);

 	        ResponseEntity<String> responseEntity = restTemplate.exchange(
 	                uploadUrl,
 	                HttpMethod.POST,
 	                requestEntity,
 	                String.class
 	        );

 	        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
 	            response.setStatus("Success");
 	            response.setMessage("Media uploaded successfully");
 	            response.setCode(HttpStatus.CREATED.value());
 	        } else {
 	            response.setStatus("Failure");
 	            response.setMessage("Failed to upload media: " + responseEntity.getStatusCode());
 	            response.setCode(responseEntity.getStatusCode().value());
 	        }
 	    } catch (Exception e) {
 	        response.setStatus("Failure");
 	        response.setMessage("Internal Server Error");
 	        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 	    }
 	    return response;
 	}

 	private ResponseStructure<String> createLinkedInPost(String mediaAsset, String caption, String mediaType) {
 	   
 	    try {                                          //urn:li:person:cHTCMRpubB
 	    	
 	    	System.out.println("controller is here 4 " + caption + " " + mediaAsset);
 	    	
 	        HttpHeaders headers = new HttpHeaders();
 	        headers.setContentType(MediaType.APPLICATION_JSON);
 	        headers.set("Authorization", "Bearer " + accessToken);

 	        String shareMediaCategory = mediaType.equals("image") ? "IMAGE" : "VIDEO";

 	        String requestBody = "{\n" +
 	                "    \"author\": \"urn:li:person:yyjwRekl0f\",\n" +
 	                "    \"lifecycleState\": \"PUBLISHED\",\n" +
 	                "    \"specificContent\": {\n" +
 	                "        \"com.linkedin.ugc.ShareContent\": {\n" +
 	                "            \"shareCommentary\": {\n" +
 	                "                \"text\": \"" + caption + "\"\n" +
 	                "            },\n" +
 	                "            \"shareMediaCategory\": \"" + shareMediaCategory + "\",\n" +
 	                "            \"media\": [\n" +
 	                "                {\n" +
 	                "                    \"status\": \"READY\",\n" +
 	                "                    \"description\": {\n" +
 	                "                        \"text\": \"Center stage!\"\n" +
 	                "                    },\n" +
 	                "                    \"media\": \"" + mediaAsset + "\",\n" +
 	                "                    \"title\": {\n" +
 	                "                        \"text\": \"LinkedIn Talent Connect 2021\"\n" +
 	                "                    }\n" +
 	                "                }\n" +
 	                "            ]\n" +
 	                "        }\n" +
 	                "    },\n" +
 	                "    \"visibility\": {\n" +
 	                "        \"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\"\n" +
 	                "    }\n" +
 	                "}";

 	        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

 	        ResponseEntity<String> responseEntity = restTemplate.exchange(
 	                "https://api.linkedin.com/v2/ugcPosts",
 	                HttpMethod.POST,
 	                requestEntity,
 	                String.class
 	        );

 	        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
 	        	System.out.println("Image with caption created successfully !!");
 	            response.setStatus("Success");
 	            response.setMessage("LinkedIn post created successfully");
 	            response.setCode(HttpStatus.CREATED.value());
 	            response.setData(responseEntity.getBody());
 	        } else {
 	            response.setStatus("Failure");
 	            response.setMessage("Failed to create LinkedIn post: " + responseEntity.getStatusCode());
 	            response.setCode(responseEntity.getStatusCode().value());
 	        }
 	    } catch (Exception e) {
 	        response.setStatus("Failure");
 	        response.setMessage("Internal Server Error");
 	        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 	    }
 	    return response;
 	}
 	
 	
 	//TEXT POST TO LINKEDIN COMPANY / PAGE
 		public ResponseStructure<String> createPostPage(String caption) {
 		    ResponseStructure<String> response = new ResponseStructure<>();
 		    try {
 		        System.out.println("Caption: " + caption);
 		        String url = "https://api.linkedin.com/v2/ugcPosts";
 		        String requestBody = "{\"author\":\"urn:li:organization:103081264\",\"lifecycleState\":\"PUBLISHED\",\"specificContent\":{\"com.linkedin.ugc.ShareContent\":{\"shareCommentary\":{\"text\":\"" + caption + "\"},\"shareMediaCategory\":\"NONE\"}},\"visibility\":{\"com.linkedin.ugc.MemberNetworkVisibility\":\"PUBLIC\"}}";

 		        HttpHeaders headers = new HttpHeaders();
 		        headers.set("Authorization", "Bearer " + accessToken);
 		        headers.set("Content-Type", "application/json");
 		        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

 		        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
 		        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
 		            response.setStatus("Success");
 		            response.setMessage("Post created successfully");
 		            response.setCode(HttpStatus.CREATED.value());
 		            response.setData(responseEntity.getBody());
 		            System.out.println("Response Body: " + responseEntity.getBody());
 		        } else {
 		            response.setStatus("Failure");
 		            response.setMessage("Failed to create post");
 		            response.setCode(responseEntity.getStatusCode().value());
 		            response.setData(responseEntity.getBody());
 		            System.out.println("Error Response: " + responseEntity.getBody());
 		        }
 		    } catch (HttpClientErrorException e) {
 		        response.setStatus("Failure");
 		        response.setMessage("HTTP Client Error: " + e.getStatusCode());
 		        response.setCode(e.getStatusCode().value());
 		        System.out.println("HttpClientErrorException: " + e.getMessage());
 		        e.printStackTrace();
 		    } catch (HttpServerErrorException e) {
 		        response.setStatus("Failure");
 		        response.setMessage("HTTP Server Error: " + e.getStatusCode());
 		        response.setCode(e.getStatusCode().value());
 		        System.out.println("HttpServerErrorException: " + e.getMessage());
 		        e.printStackTrace();
 		    } catch (Exception e) {
 		        response.setStatus("Failure");
 		        response.setMessage("Internal Server Error: " + e.getMessage());
 		        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 		        System.out.println("Exception: " + e.getMessage());
 		        e.printStackTrace();
 		    }
 		    return response;
 		}
 		
 		
 	// SHARE IMAGE/VIDEO AND TEXT TO LINKEDIN PAGE/ORGANIZATION
 	   public ResponseStructure<String> uploadImageToLinkedInPage(MultipartFile file, String caption) {
 		   
 		    try {
 		    	
 		    	System.out.println("controller is here 1 " + caption + " " + file);
 		    	
 		        String recipeType = determineRecipeTypePage(file);
 		        String mediaType = determineMediaTypePage(file);
 		        JsonNode uploadResponse = registerUploadPage(recipeType);
 		        String uploadUrl = uploadResponse.get("value").get("uploadMechanism").get("com.linkedin.digitalmedia.uploading.MediaUploadHttpRequest").get("uploadUrl").asText();
 		        String mediaAsset = uploadResponse.get("value").get("asset").asText();
 		        uploadImagePage(uploadUrl, file);
 		        ResponseStructure<String> postResponse = createLinkedInPostPage(mediaAsset, caption, mediaType);
 		        response.setStatus("Success");
 		        response.setMessage("Media uploaded successfully");
 		        response.setCode(HttpStatus.CREATED.value());
 		        response.setData(postResponse.getData());
 		    } catch (IOException e) {
 		        response.setStatus("Failure");
 		        response.setMessage("Failed to upload media to LinkedIn: " + e.getMessage());
 		        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 		    }
 		    return response;
 		}

 	   private String determineRecipeTypePage(MultipartFile file) {
 	       String contentType = file.getContentType();
 	       return contentType != null && contentType.startsWith("image") ? "urn:li:digitalmediaRecipe:feedshare-image" : "urn:li:digitalmediaRecipe:feedshare-video";
 	   }

 	   private String determineMediaTypePage(MultipartFile file) {
 	       return file.getContentType() != null && file.getContentType().startsWith("image") ? "image" : "video";
 	   }

 	   private JsonNode registerUploadPage(String recipeType) throws IOException {
 		   
 		   System.out.println("controller is here 2 " + recipeType);
 		   
 	       HttpHeaders headers = new HttpHeaders();
 	       headers.setContentType(MediaType.APPLICATION_JSON);
 	       headers.set("Authorization", "Bearer " + accessToken);								// urn:li:person:cHTCMRpubB

 	       String requestBody = "{\"registerUploadRequest\": {\"recipes\": [\"" + recipeType + "\"],\"owner\": \"urn:li:organization:103081264\",\"serviceRelationships\": [{\"relationshipType\": \"OWNER\",\"identifier\": \"urn:li:userGeneratedContent\"}]}}";

 	       HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

 	       ResponseEntity<JsonNode> responseEntity = restTemplate.exchange(
 	               "https://api.linkedin.com/v2/assets?action=registerUpload",
 	               HttpMethod.POST,
 	               requestEntity,
 	               JsonNode.class
 	       );

 	       if (responseEntity.getStatusCode() == HttpStatus.OK) {
 	           return responseEntity.getBody();
 	       } else {
 	           throw new RuntimeException("Failed to register upload: " + responseEntity.getStatusCode());
 	       }
 	   }

 	   private ResponseStructure<String> uploadImagePage(String uploadUrl, MultipartFile file) {
 		  
 		    try {
 		    	
 		    	System.out.println("controller is here 3 " + uploadUrl + " " + file);
 		    	
 		        HttpHeaders headers = new HttpHeaders();
 		        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
 		        headers.set("Authorization", "Bearer " + accessToken);

 		        byte[] fileContent;
 		        try {
 		            fileContent = file.getBytes();
 		        } catch (IOException e) {
 		            response.setStatus("Failure");
 		            response.setMessage("Failed to read image file");
 		            response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 		            return response;
 		        }

 		        HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContent, headers);

 		        ResponseEntity<String> responseEntity = restTemplate.exchange(
 		                uploadUrl,
 		                HttpMethod.POST,
 		                requestEntity,
 		                String.class
 		        );

 		        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
 		            response.setStatus("Success");
 		            response.setMessage("Media uploaded successfully");
 		            response.setCode(HttpStatus.CREATED.value());
 		        } else {
 		            response.setStatus("Failure");
 		            response.setMessage("Failed to upload media: " + responseEntity.getStatusCode());
 		            response.setCode(responseEntity.getStatusCode().value());
 		        }
 		    } catch (Exception e) {
 		        response.setStatus("Failure");
 		        response.setMessage("Internal Server Error");
 		        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 		    }
 		    return response;
 		}

 		private ResponseStructure<String> createLinkedInPostPage(String mediaAsset, String caption, String mediaType) {
 		   
 		    try {                                          //urn:li:person:cHTCMRpubB
 		    	
 		    	System.out.println("controller is here 4 " + caption + " " + mediaAsset);
 		    	
 		        HttpHeaders headers = new HttpHeaders();
 		        headers.setContentType(MediaType.APPLICATION_JSON);
 		        headers.set("Authorization", "Bearer " + accessToken);

 		        String shareMediaCategory = mediaType.equals("image") ? "IMAGE" : "VIDEO";

 		        String requestBody = "{\n" +
 		                "    \"author\": \"urn:li:organization:103081264\",\n" +
 		                "    \"lifecycleState\": \"PUBLISHED\",\n" +
 		                "    \"specificContent\": {\n" +
 		                "        \"com.linkedin.ugc.ShareContent\": {\n" +
 		                "            \"shareCommentary\": {\n" +
 		                "                \"text\": \"" + caption + "\"\n" +
 		                "            },\n" +
 		                "            \"shareMediaCategory\": \"" + shareMediaCategory + "\",\n" +
 		                "            \"media\": [\n" +
 		                "                {\n" +
 		                "                    \"status\": \"READY\",\n" +
 		                "                    \"description\": {\n" +
 		                "                        \"text\": \"Center stage!\"\n" +
 		                "                    },\n" +
 		                "                    \"media\": \"" + mediaAsset + "\",\n" +
 		                "                    \"title\": {\n" +
 		                "                        \"text\": \"LinkedIn Talent Connect 2021\"\n" +
 		                "                    }\n" +
 		                "                }\n" +
 		                "            ]\n" +
 		                "        }\n" +
 		                "    },\n" +
 		                "    \"visibility\": {\n" +
 		                "        \"com.linkedin.ugc.MemberNetworkVisibility\": \"PUBLIC\"\n" +
 		                "    }\n" +
 		                "}";

 		        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

 		        ResponseEntity<String> responseEntity = restTemplate.exchange(
 		                "https://api.linkedin.com/v2/ugcPosts",
 		                HttpMethod.POST,
 		                requestEntity,
 		                String.class
 		        );

 		        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
 		        	System.out.println("Image with caption created successfully !!");
 		            response.setStatus("Success");
 		            response.setMessage("LinkedIn post created successfully");
 		            response.setCode(HttpStatus.CREATED.value());
 		            response.setData(responseEntity.getBody());
 		        } else {
 		            response.setStatus("Failure");
 		            response.setMessage("Failed to create LinkedIn post: " + responseEntity.getStatusCode());
 		            response.setCode(responseEntity.getStatusCode().value());
 		        }
 		    } catch (Exception e) {
 		        response.setStatus("Failure");
 		        response.setMessage("Internal Server Error");
 		        response.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
 		    }
 		    return response;
 		}
}


