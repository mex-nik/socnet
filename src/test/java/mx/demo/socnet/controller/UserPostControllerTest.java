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

import mx.demo.socnet.data.entity.Roles;
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.entity.UserPost;
import mx.demo.socnet.service.UserDataService;
import mx.demo.socnet.service.UserPostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 25.06.2021
 * @project socnet
 */

@WebMvcTest(UserPostController.class)
public class UserPostControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserPostService userPostService;

    @MockBean
    private UserDataService userDataService;

    @MockBean
    private UserDetailsService userDetailsService;

	private static UserData adminUserData;

	private static UserData regularUserData;

    @BeforeAll
    public static void setup() {
       adminUserData = new UserData(0L, "Admin", "Adminson", "admin@admins.net",
               new Roles(Roles.ADMIN), "admin", "Adminland", "admin",
               new ArrayList<>(Arrays.asList(new UserPost(0L, adminUserData, "Hello, I'm Admin", new Date(Calendar.getInstance().getTimeInMillis())),
                       new UserPost(1L, adminUserData, "Hello, I'm still Admin", new Date(Calendar.getInstance().getTimeInMillis())))));
        regularUserData = new UserData(1L, "Regular", "Guy", "guy@regulars.net",
                new Roles(Roles.REGULAR), "regular", "Reguland", "regular",
                new ArrayList<>(Arrays.asList(new UserPost(0L, regularUserData, "Hello, I'm not Admin", new Date(Calendar.getInstance().getTimeInMillis())),
                        new UserPost(1L, regularUserData, "Hello, I'm just Regular Guy", new Date(Calendar.getInstance().getTimeInMillis())))));
    }
    
    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void addPostRegularSelfPost() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);

        UserPost addMe = new UserPost(6L, regularUserData, "TestADD", new Date(Calendar.getInstance().getTimeInMillis()));

        Mockito.doAnswer(invocationOnMock -> {
            regularUserData.getPosts().add(addMe);
            return invocationOnMock;
        }).when(userPostService).addPost(addMe);

        this.mockMvc.perform(post("/addPost").with(csrf()).flashAttr("newpost", addMe)
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(addMe.getPost())));
        regularUserData.getPosts().removeIf(post -> post.getId().equals(addMe.getId()));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void addPostAdminSelfPost() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        UserPost addMe = new UserPost(6L, adminUserData, "TestADD", new Date(Calendar.getInstance().getTimeInMillis()));

        Mockito.doAnswer(invocationOnMock -> {
            adminUserData.getPosts().add(addMe);
            return invocationOnMock;
        }).when(userPostService).removePost(addMe);

        this.mockMvc.perform(post("/addPost").with(csrf()).flashAttr("newpost", addMe)
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(addMe.getPost())));
        adminUserData.getPosts().removeIf(post -> post.getId().equals(addMe.getId()));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void addPostRegularOtherPost() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);
        UserData otherUser = new UserData();
        otherUser.setId(regularUserData.getId() + 3L);
        UserPost othersPost = new UserPost(0L, otherUser, "Hello, I'm not Admin", new Date(Calendar.getInstance().getTimeInMillis()));

        this.mockMvc.perform(post("/addPost").with(csrf()).flashAttr("newpost", othersPost)
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void addPostAdminOtherPost() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        UserData otherUser = new UserData();
        otherUser.setId(adminUserData.getId() + 3L);
        UserPost othersPost = new UserPost(0L, otherUser, "Hello, I'm not Admin", new Date(Calendar.getInstance().getTimeInMillis()));

        this.mockMvc.perform(post("/addPost").with(csrf()).flashAttr("newpost", othersPost)
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void deletePostRegularSelfPost() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);
        UserPost removeMe = new UserPost(6L, regularUserData, "TestRemove", new Date(Calendar.getInstance().getTimeInMillis()));
        regularUserData.getPosts().add(removeMe);
        Mockito.doAnswer(invocationOnMock -> {
            regularUserData.getPosts().remove(removeMe);
            return invocationOnMock;
        }).when(userPostService).removePost(removeMe);

        this.mockMvc.perform(post("/deletePost").with(csrf()).flashAttr("post", removeMe)
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(removeMe.getPost()))));
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void deletePostAdminSelfPost() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        UserPost removeMe = new UserPost(6L, adminUserData, "TestRemove", new Date(Calendar.getInstance().getTimeInMillis()));
        adminUserData.getPosts().add(removeMe);
        Mockito.doAnswer(invocationOnMock -> {
            adminUserData.getPosts().remove(removeMe);
            return invocationOnMock;
        }).when(userPostService).removePost(removeMe);

        this.mockMvc.perform(post("/deletePost").with(csrf()).flashAttr("post", removeMe)
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(removeMe.getPost()))));
    }

    @WithMockUser(username = "guy@regulars.net", roles = Roles.REGULAR)
    @Test
    public void deletePostRegularOtherPost() throws Exception {
        Mockito.when(userDataService.getUser(regularUserData.getEmail())).thenReturn(regularUserData);

        UserData otherUser = new UserData();
        otherUser.setId(regularUserData.getId() + 3L);
        UserPost othersPost = new UserPost(0L, otherUser, "Hello, I'm not Admin", new Date(Calendar.getInstance().getTimeInMillis()));

        this.mockMvc.perform(post("/deletePost").with(csrf()).flashAttr("post", othersPost)
                .sessionAttr("user", regularUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(username = "admin@admins.net", roles = Roles.ADMIN)
    @Test
    public void deletePostAdminOtherPost() throws Exception {
        Mockito.when(userDataService.getUser(adminUserData.getEmail())).thenReturn(adminUserData);

        UserPost removeMe = new UserPost(6L, regularUserData, "TestRemove", new Date(Calendar.getInstance().getTimeInMillis()));
        regularUserData.getPosts().add(removeMe);
        Mockito.doAnswer(invocationOnMock -> {
            regularUserData.getPosts().remove(removeMe);
            return invocationOnMock;
        }).when(userPostService).removePost(removeMe);

        this.mockMvc.perform(post("/deletePost").with(csrf()).flashAttr("post", removeMe)
                .sessionAttr("user", adminUserData))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(removeMe.getPost()))));
    }

}
