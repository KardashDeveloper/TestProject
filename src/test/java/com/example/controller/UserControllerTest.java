package com.example.controller;

import com.example.DemoApplication;
import com.example.entity.User;
import com.example.entity.requests.AddUserRequest;
import com.example.entity.requests.LoginRequest;
import com.example.repository.UserRepository;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DemoApplication.class)
@WebAppConfiguration
public class UserControllerTest {
    @Autowired
    private UserRepository userRepository;
    @Rule
    public JUnitRestDocumentation restDocumentation =
            new JUnitRestDocumentation("build/generated-snippets");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void getAll() throws Exception {
        this.mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andDo(document("index"));
    }

    @Test
    public void addUser() throws Exception {
        AddUserRequest addUserRequest = new AddUserRequest("Mike", "Smith", "mike@gmail.com", "12345");
        this.mockMvc.perform(post("/api/user").
                contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(addUserRequest).toString()).
                accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).
                andDo(document("adduser_created"));

    }

    @Test
    public void login() throws Exception {
        AddUserRequest addUserRequest = new AddUserRequest("Mike", "Smith", "mike@gmail.com", "12345");
        this.mockMvc.perform(post("/api/user").
                contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(addUserRequest).toString()).
                accept(MediaType.APPLICATION_JSON));
        LoginRequest loginRequest = new LoginRequest("mike@gmail.com", "12345");
        this.mockMvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(loginRequest).toString()).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andDo(document("login_ok"));
    }

    @Test
    public void getUserByEmail() throws Exception {
        AddUserRequest addUserRequest = new AddUserRequest("Mike", "Smith", "mike@gmail.com", "12345");
        this.mockMvc.perform(post("/api/user").
                contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(addUserRequest).toString()).
                accept(MediaType.APPLICATION_JSON));
        this.mockMvc.perform(get("/api/user/mike@gmail.com")
                .accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andDo(document("getUserByEmail_ok"));
    }

    @Test
    public void saveImage() throws Exception {
        File f = new File(System.getProperty("user.dir") + "/src/test/images/1.jpg");
        FileInputStream fileInputStream = new FileInputStream(f);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", f.getName(), "multipart/form-data", fileInputStream);
        User user = new User("Mike", "Smith", "mike@gmail.com", "12345");
        userRepository.save(user);
        UUID uuid = user.getUuid();
        this.mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/user/" + uuid + "/receipts").
                file(mockMultipartFile)).andExpect(status().isOk()).
                andDo(document("saveImage_ok"));

    }

    //adding existing user, expect Conflict
    @Test
    public void addExistingUser() throws Exception {
        AddUserRequest addUserRequest = new AddUserRequest("Mike", "Smith", "Smith@gmail.com", "12345");
        this.mockMvc.perform(post("/api/user").
                contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(addUserRequest).toString()).
                accept(MediaType.APPLICATION_JSON));
        this.mockMvc.perform(post("/api/user").
                contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(addUserRequest).toString()).
                accept(MediaType.APPLICATION_JSON)).andExpect(status().isConflict()).
                andDo(document("adduser_conflict"));
    }

    //adding user, but not specifying or giving empty field, expect Bad Request
    @Test
    public void addUserUnProperly() throws Exception {
        AddUserRequest addUserRequest = new AddUserRequest("", "Smith", "smith@gmail", "");
        this.mockMvc.perform(post("/api/user").
                contentType(MediaType.APPLICATION_JSON).
                content(new JSONObject(addUserRequest).toString())).andExpect(status().isBadRequest()).
                andDo(document("adduser_badrequest"));

    }

    //login (no such user), expect Bad Request
    @Test
    public void loginByWrongEmail() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", "mike@gmail.com.wrong.email");
        jsonObject.put("password", "12345");
        this.mockMvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON).
                content(jsonObject.toString()).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest()).andDo(document("login_wrongemail"));
    }

    //login (wrong password), expect Bad Request
    @Test
    public void loginByWrongPassword() throws Exception {
        User user = new User("Mike", "Smith", "mike@gmail.com", "12345");
        userRepository.save(user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("email", "mike@gmail.com");
        jsonObject.put("password", "wrong password");
        this.mockMvc.perform(post("/api/user/login").contentType(MediaType.APPLICATION_JSON).
                content(jsonObject.toString()).accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest()).andDo(document("login_wrongpassword"));
        userRepository.delete(user.getUuid());
    }

    //get user by wrong email , expect Bad Request
    @Test
    public void getUserByWrongEmail() throws Exception {
        this.mockMvc.perform(get("/api/user/wrong@email")
                .accept(MediaType.APPLICATION_JSON)).
                andExpect(status().isBadRequest()).andDo(document("getUserByEmail_badrequest"));
    }

    //uploading file, giving wrong uuid expect Bad Request
    @Test
    public void saveImageByWrongUuid() throws Exception {
        File f = new File(System.getProperty("user.dir") + "/src/test/images/2.jpg");
        FileInputStream fileInputStream = new FileInputStream(f);
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", f.getName(), "multipart/form-data", fileInputStream);
        //User user = new User("Mike","Smith","mike@gmail.com","12345");
        //userRepository.save(user);
        //UUID uuid = user.getUuid();
        this.mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/user/wrong_uuid/receipts").
                file(mockMultipartFile)).andExpect(status().isBadRequest()).
                andDo(document("saveImage_wronguuid"));

    }
}
