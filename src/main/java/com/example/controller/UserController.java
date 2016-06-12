package com.example.controller;

import com.example.entity.Receipt;
import com.example.entity.User;
import com.example.entity.requests.AddUserRequest;
import com.example.entity.requests.LoginRequest;
import com.example.repository.ReceiptRepository;
import com.example.repository.UserRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Kardash on 07.06.2016.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addUser(@RequestBody AddUserRequest addUserRequest) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (userRepository.findByEmailContaining(addUserRequest.getEmail()) != null) {
                jsonObject.put("message", "There already exists user with such email.");
                return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CONFLICT);
            }
            User user = new User();
            user.setFirstName(addUserRequest.getFirstName());
            user.setLastName(addUserRequest.getLastName());
            user.setEmail(addUserRequest.getEmail());
            user.setPassword(addUserRequest.getPassword());
            if (!user.allBasicFieldsNotEmpty()) {
                jsonObject.put("message", "Fields are empty or not specified.");
                return new ResponseEntity<>(jsonObject.toString(), HttpStatus.BAD_REQUEST);
            }
            userRepository.save(user);
            jsonObject = new JSONObject();
            jsonObject.put("uuid", user.getUuid());
            return new ResponseEntity<>(jsonObject.toString(), HttpStatus.CREATED);
        } catch (JSONException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            JSONObject jsonObject = new JSONObject();
            User user = userRepository.findByEmailContaining(loginRequest.getEmail());
            if (user != null) {
                if (user.getPassword().equals(loginRequest.getPassword())) {
                    SecureRandom random = new SecureRandom();
                    byte bytes[] = new byte[30];
                    random.nextBytes(bytes);
                    String token = bytes.toString();
                    jsonObject.put("token", token);
                    return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);
                } else {
                    jsonObject.put("message", "Invalid password.");
                    return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.BAD_REQUEST);
                }
            } else {
                jsonObject.put("message", "There is no such user.");
                return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.BAD_REQUEST);
            }
        } catch (JSONException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{email}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getUserByEmail(@PathVariable("email") String email) {
        try {
            JSONObject jsonObject = new JSONObject();
            User user = userRepository.findByEmailContaining(email);
            if (user != null) {
                jsonObject.put("firstName", user.getFirstName());
                jsonObject.put("lastName", user.getLastName());
                return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.OK);
            } else {
                jsonObject.put("message", "Can't find user by given email.");
                return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.BAD_REQUEST);
            }
        } catch (JSONException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getAll() {
        return userRepository.findAll();
    }


    @RequestMapping(value = "/{uuid}/receipts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveImage(@PathVariable("uuid") UUID uuid,
                                            @RequestPart("file") MultipartFile multipartFile) {
        JSONObject jsonObject = new JSONObject();
        try {
            User user = userRepository.findOne(uuid);
            if (user == null) {
                jsonObject.put("message", "Can't find user by given uuid");
                return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.BAD_REQUEST);
            }
            if (!multipartFile.isEmpty()) {
                File dir = new File(System.getProperty("user.dir") + "/src/main/resources/images/" + multipartFile.getOriginalFilename());
                String path = dir.getAbsolutePath();
                try {
                    if (!dir.exists()) {
                        dir.createNewFile();
                    }
                    BufferedOutputStream stream = new BufferedOutputStream(
                            new FileOutputStream(dir));
                    FileCopyUtils.copy(multipartFile.getInputStream(), stream);
                    stream.close();
                } catch (IOException e) {
                    jsonObject.put("message", "Can't save file on server.");
                    return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
                Receipt receipt = new Receipt();
                receipt.setAddress(path);
                receipt.setUploadDate(new Date());
                user.addReceipt(receipt);
                receiptRepository.save(receipt);
                return new ResponseEntity<String>(HttpStatus.OK);
            } else
                jsonObject.put("message", "File is empty.");
            return new ResponseEntity<String>(jsonObject.toString(), HttpStatus.BAD_REQUEST);
        } catch (JSONException e) {
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
