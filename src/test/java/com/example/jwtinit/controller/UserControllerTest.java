package com.example.jwtinit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
//@Rollback(false)
public class UserControllerTest {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document("{method-name}/{step}/"))
                .build();
    }

    private String keyword = "test";

    @Test
    @DisplayName("회원가입 테스트")
    public void signup() throws Exception {

        this.mockMvc
                .perform(
                         post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(getReqParams(keyword))) // Map to Json Request Param
                ).andExpect(status().isOk()) // http 200
//                ).andExpect(status().is4xxClientError())
                .andDo(print())
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    @DisplayName("로그인 권한 검증 테스트")
    public void authenticate() throws Exception {

        signup();

        this.mockMvc
                .perform(
                     post("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(getReqParams(keyword)))
                ).andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    @DisplayName("접근 권한 테스트: ALL")
    public void user() throws Exception {

        authenticate();

        this.mockMvc
                .perform(
                    get("/api/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(getReqParams(keyword)))
                ).andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getHeader("Location");
    }

    @Test
    @DisplayName("접근 권한 테스트: ADMIN")
    public void userAdmin() throws Exception {

        authenticate();

        this.mockMvc
                .perform(get("/api/user/{username}", "admin").contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("content-type", "application/json"))
//                .andExpect(jsonPath("$.authorities..['authorityName']").value(Arrays.asList("ROLE_ADMIN")))
                .andDo(print())
                .andReturn().getResponse().getHeader("Location")
        ;
    }

    public Map<String, String> getReqParams(String keyword) {

        Map<String, String> reqParams = new HashMap<>();

        reqParams.put("username", keyword);
        reqParams.put("password", keyword);
        reqParams.put("nickname", keyword);

        return reqParams;
    }

}