/*
 * Copyright (c) 2021 Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.demo.socnet.controller;

import mx.demo.socnet.controller.UserDataController;
import mx.demo.socnet.data.entity.Roles;
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.entity.UserPost;
import mx.demo.socnet.service.UserDataService;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 21.06.2021
 * @project socnet
 */

@WebMvcTest(UserDataController.class)
public class UserDataControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserDataService userDataService;

    @MockBean
    private UserDetailsService userDetailsService;

    private static List<UserData> userDataList = new ArrayList<>();
    private static Page<UserData> userDataPage;
    private static UserData adminUserData;
    private static UserData regularUserData;
    private static List<String> matcherStringsDirectoryAdmin = new ArrayList<>();
    private static List<Matcher<String>> matcherRegularUserSelf = new ArrayList<>();
    private static List<Matcher<String>> matcherAdminUserSelf = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        adminUserData = new UserData(0L, "Admin", "Adminson", "admin@admins.net",
                new Roles(Roles.ADMIN), "admin", "Adminland", "admin",
                new ArrayList<>(Arrays.asList(new UserPost(0L, adminUserData, "Hello, I am Admin", new Date(Calendar.getInstance().getTimeInMillis())),
                        new UserPost(1L, adminUserData, "Hello, I am still Admin", new Date(Calendar.getInstance().getTimeInMillis())))));
        regularUserData = new UserData(1L, "Regular", "Guy", "guy@regulars.net",
                new Roles(Roles.REGULAR), "regular", "Reguland", "regular",
                new ArrayList<>(Arrays.asList(new UserPost(0L, regularUserData, "Hello, I am not Admin", new Date(Calendar.getInstance().getTimeInMillis())),
                        new UserPost(1L, regularUserData, "Hello, I am just Regular Guy", new Date(Calendar.getInstance().getTimeInMillis())))));
        
        userDataList.add(adminUserData);
        userDataList.add(regularUserData);

        matcherAdminUserSelf.add(Matchers.containsString(adminUserData.getFirstName()));
        matcherAdminUserSelf.add(Matchers.containsString(adminUserData.getLastName()));
        matcherAdminUserSelf.add(Matchers.containsString(adminUserData.getEmail()));
        matcherAdminUserSelf.add(Matchers.containsString(adminUserData.getGender()));
        matcherAdminUserSelf.add(Matchers.containsString(adminUserData.getCountry()));
        adminUserData.getPosts().stream().forEach(post -> {
            matcherAdminUserSelf.add(Matchers.containsString(post.getPublished().toString()));
            matcherAdminUserSelf.add(Matchers.containsString(post.getPost()));
        });

        matcherRegularUserSelf.add(Matchers.containsString(regularUserData.getFirstName()));
        matcherRegularUserSelf.add(Matchers.containsString(regularUserData.getLastName()));
        matcherRegularUserSelf.add(Matchers.containsString(regularUserData.getEmail()));
        matcherRegularUserSelf.add(Matchers.containsString(regularUserData.getGender()));
        matcherRegularUserSelf.add(Matchers.containsString(regularUserData.getCountry()));
        regularUserData.getPosts().stream().forEach(post -> {
            matcherRegularUserSelf.add(Matchers.containsString(post.getPublished().toString()));
            matcherRegularUserSelf.add(Matchers.containsString(post.getPost()));
        });

        matcherStringsDirectoryAdmin.add(regularUserData.getFirstName());
        matcherStringsDirectoryAdmin.add(regularUserData.getLastName());
        matcherStringsDirectoryAdmin.add(regularUserData.getEmail());
        matcherStringsDirectoryAdmin.add("<form method=\"GET\" action=\"/editUser\">");
        matcherStringsDirectoryAdmin.add("<a class=\"btn btn-light\" href=\"/deleteUser?userId=");
        
        IntStream.rangeClosed(3, 10).asLongStream().forEach(index -> {
            Roles role = (index % 2 == 0 ? new Roles(Roles.ADMIN) : new Roles(Roles.REGULAR));
            UserData userData = new UserData(index, "firstName" + index, "lastName" + index, "email" + index,
                    role, "gender" + index, "country" + index,
                    "password" + index, null);
            userData.setPosts(Arrays.asList(new UserPost(0L, userData, "post0", new Date(Calendar.getInstance().getTimeInMillis())),
                                            new UserPost(1L, userData, "post1", new Date(Calendar.getInstance().getTimeInMillis()))));
            userDataList.add(userData);
            matcherStringsDirectoryAdmin.add(userData.getFirstName());
            matcherStringsDirectoryAdmin.add(userData.getLastName());
            matcherStringsDirectoryAdmin.add(userData.getEmail());
            matcherStringsDirectoryAdmin.add("<form method=\"GET\" action=\"/editUser\">");
            matcherStringsDirectoryAdmin.add("<a class=\"btn btn-light\" href=\"/deleteUser?userId=");

        });

        userDataPage = new PageImpl<>(userDataList);
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void directoryRegularGet() throws Exception {
        Mockito.when(userDataService.getUsersPage(0, 20)).thenReturn(userDataPage);

        this.mockMvc.perform(get("/directory")
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string(Matchers.not(Matchers.anyOf(Matchers.containsString("<a class=\"btn btn-light\" href=\"/deleteUser?userId="),
                                                            Matchers.containsString("<form method=\"GET\" action=\"/editUser\">")))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void directoryAdminGet() throws Exception {
        Mockito.when(userDataService.getUsersPage(0, 20)).thenReturn(userDataPage);

        this.mockMvc.perform(get("/directory")
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(matcherStringsDirectoryAdmin)));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void userRegularSelfGet() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);
        this.mockMvc.perform(get("/user")
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.allOf(matcherRegularUserSelf.stream().collect(Collectors.toList()))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void userAdminSelfGet() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);
        this.mockMvc.perform(get("/user")
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.allOf(matcherAdminUserSelf.stream().collect(Collectors.toList()))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void userRegularOtherGet() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getId())).thenReturn(adminUserData);
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);

        this.mockMvc.perform(get("/user").param("userId", adminUserData.getId().toString())
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("deletePost"))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void userAdminOtherGet() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getId())).thenReturn(regularUserData);
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        this.mockMvc.perform(get("/user").param("userId", regularUserData.getId().toString())
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("deletePost")));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void userRegularSelfPost() throws Exception {
        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", regularUserData)
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.allOf(matcherRegularUserSelf.stream().collect(Collectors.toList()))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void userAdminSelfPost() throws Exception {
        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", adminUserData)
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.allOf(matcherAdminUserSelf.stream().collect(Collectors.toList()))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void userRegularOtherPost() throws Exception {

        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", adminUserData)
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("deletePost"))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void userAdminOtherPost() throws Exception {

        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", regularUserData)
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("deletePost")));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void newUserRegularGet() throws Exception {
        this.mockMvc.perform(get("/newUser").with(csrf()).param("userId", adminUserData.getId().toString())
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void newUserAdminGet() throws Exception {
        this.mockMvc.perform(get("/newUser")
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void editUserRegularSelfGet() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);
        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(regularUserData.getFirstName());
        matcherRegularUserSelf.add(regularUserData.getLastName());
        matcherRegularUserSelf.add(regularUserData.getEmail());
        matcherRegularUserSelf.add(regularUserData.getGender());
        matcherRegularUserSelf.add(regularUserData.getCountry());

        this.mockMvc.perform(get("/editUser")
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.not(Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>")))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void editUserAdminSelfGet() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);
        List<String> matcherAdminUserSelf = new ArrayList<>();
        matcherAdminUserSelf.add(adminUserData.getFirstName());
        matcherAdminUserSelf.add(adminUserData.getLastName());
        matcherAdminUserSelf.add(adminUserData.getEmail());
        matcherAdminUserSelf.add(adminUserData.getGender());
        matcherAdminUserSelf.add(adminUserData.getCountry());

        this.mockMvc.perform(get("/editUser")
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherAdminUserSelf),
                                Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>"))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void editUserRegularOtherGet() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);

        this.mockMvc.perform(get("/editUser").param("user", adminUserData.toString())
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void editUserAdminOtherGet() throws Exception {
        Long otherUserId = adminUserData.getId() + 1L;
        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getFirstName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getLastName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getEmail());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getGender());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getCountry());
        
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        this.mockMvc.perform(get("/editUser").param("user", userDataList.get(otherUserId.intValue()).toString())
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>"))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void editUserRegularSelfPost() throws Exception {
        Long otherUserId = regularUserData.getId() + 1L;
        UserData updatedUser = new UserData(regularUserData.getId(), userDataList.get(otherUserId.intValue()).getFirstName(),
                                            userDataList.get(otherUserId.intValue()).getLastName(), userDataList.get(otherUserId.intValue()).getEmail(),
                                            regularUserData.getRole(), userDataList.get(otherUserId.intValue()).getGender(),
                                            userDataList.get(otherUserId.intValue()).getCountry(), userDataList.get(otherUserId.intValue()).getPassword(),
                                            userDataList.get(otherUserId.intValue()).getPosts());

        Mockito.when(userDataService.updateUser(updatedUser)).thenReturn(updatedUser);
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);


        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(updatedUser.getFirstName());
        matcherRegularUserSelf.add(updatedUser.getLastName());
        matcherRegularUserSelf.add(updatedUser.getEmail());
        matcherRegularUserSelf.add(updatedUser.getGender());
        matcherRegularUserSelf.add(updatedUser.getCountry());

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", updatedUser)
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.not(Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>")))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void editUserAdminSelfPost() throws Exception {
        Long otherUserId = adminUserData.getId() + 1L;
        UserData updatedUser = new UserData(adminUserData.getId(), userDataList.get(otherUserId.intValue()).getFirstName(),
                userDataList.get(otherUserId.intValue()).getLastName(), userDataList.get(otherUserId.intValue()).getEmail(),
                adminUserData.getRole(), userDataList.get(otherUserId.intValue()).getGender(),
                userDataList.get(otherUserId.intValue()).getCountry(), userDataList.get(otherUserId.intValue()).getPassword(),
                userDataList.get(otherUserId.intValue()).getPosts());

        Mockito.when(userDataService.updateUser(updatedUser)).thenReturn(updatedUser);
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);


        List<String> matcherAdminUserSelf = new ArrayList<>();
        matcherAdminUserSelf.add(updatedUser.getFirstName());
        matcherAdminUserSelf.add(updatedUser.getLastName());
        matcherAdminUserSelf.add(updatedUser.getEmail());
        matcherAdminUserSelf.add(updatedUser.getGender());
        matcherAdminUserSelf.add(updatedUser.getCountry());

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", updatedUser)
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherAdminUserSelf),
                                Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>"))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void editUserRegularSelfRolePost() throws Exception {
        Long otherUserId = regularUserData.getId() + 1L;
        UserData updatedUser = new UserData(regularUserData.getId(), userDataList.get(otherUserId.intValue()).getFirstName(),
                userDataList.get(otherUserId.intValue()).getLastName(), userDataList.get(otherUserId.intValue()).getEmail(),
                new Roles(Roles.ADMIN), userDataList.get(otherUserId.intValue()).getGender(),
                userDataList.get(otherUserId.intValue()).getCountry(), userDataList.get(otherUserId.intValue()).getPassword(),
                userDataList.get(otherUserId.intValue()).getPosts());
        
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", updatedUser)
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void editUserAdminSelfRolePost() throws Exception {
        Long otherUserId = adminUserData.getId() + 1L;
        UserData updatedUser = new UserData(adminUserData.getId(), userDataList.get(otherUserId.intValue()).getFirstName(),
                userDataList.get(otherUserId.intValue()).getLastName(), userDataList.get(otherUserId.intValue()).getEmail(),
                new Roles(Roles.REGULAR), userDataList.get(otherUserId.intValue()).getGender(),
                userDataList.get(otherUserId.intValue()).getCountry(), userDataList.get(otherUserId.intValue()).getPassword(),
                userDataList.get(otherUserId.intValue()).getPosts());

        Mockito.when(userDataService.updateUser(updatedUser)).thenReturn(updatedUser);
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);
        Mockito.when(userDataService.getUser(updatedUser.getEmail())).thenReturn(updatedUser);


        List<String> matcherAdminUserSelf = new ArrayList<>();
        matcherAdminUserSelf.add(updatedUser.getFirstName());
        matcherAdminUserSelf.add(updatedUser.getLastName());
        matcherAdminUserSelf.add(updatedUser.getEmail());
        matcherAdminUserSelf.add(updatedUser.getGender());
        matcherAdminUserSelf.add(updatedUser.getCountry());

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", updatedUser)
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherAdminUserSelf),
                                Matchers.not(Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>")))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void editUserRegularOtherPost() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", adminUserData)
                .sessionAttr("user", regularUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void editUserAdminOtherPost() throws Exception {
        Long otherUserId = adminUserData.getId() + 1L;

        Mockito.when(userDataService.updateUser(userDataList.get(otherUserId.intValue()))).thenReturn(userDataList.get(otherUserId.intValue()));
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getFirstName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getLastName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getEmail());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getGender());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getCountry());

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", userDataList.get(otherUserId.intValue()))
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.containsString("value=\"regular\" selected=\"selected\""),
                                Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>"))));

    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void deleteUserRegularGet() throws Exception {

        this.mockMvc.perform(get("/deleteUser").with(csrf()).param("userId", adminUserData.getId().toString())
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void deleteUserAdminGet() throws Exception {
        Long otherUserId = adminUserData.getId() + 1L;
        String email = userDataList.get(otherUserId.intValue()).getEmail();
        Mockito.when(userDataService.getUser(otherUserId)).thenReturn(userDataList.get(otherUserId.intValue()));
        Mockito.when(userDataService.getUsersPage(0, 20)).thenReturn(userDataPage);
        Mockito.doAnswer(invocationOnMock -> {
            List<UserData> userDataListRemovedUser = new ArrayList<>(userDataList);
            userDataListRemovedUser.remove(otherUserId.intValue());
            Page<UserData> userDataPage = new PageImpl<>(userDataListRemovedUser);
            Mockito.when(userDataService.getUsersPage(0, 20)).thenReturn(userDataPage);
            return invocationOnMock;
        }).when(userDataService).deleteUserById(otherUserId);

        this.mockMvc.perform(get("/deleteUser").with(csrf()).param("userId", otherUserId.toString())
                .sessionAttr("user", adminUserData)).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(email))));
    }
}
