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

package mx.demo.socnet.service;

import mx.demo.socnet.controller.UserDataController;
import mx.demo.socnet.data.entity.Roles;
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.entity.UserPost;
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
    private static List<String> matcherStringsDirectoryAdmin = new ArrayList<>();
    private static List<Matcher<String>> matcherRegularUserSelf = new ArrayList<>(10);
    private static int loggedInAdminUserIndex = 4;
    private static int loggedInRegularUserIndex = 3;

    @BeforeAll
    public static void setup() {
        IntStream.rangeClosed(0, 10).asLongStream().forEach(index -> {
            Roles role = (index % 2 == 0 ? new Roles(Roles.ADMIN) : new Roles(Roles.REGULAR));
            UserData userData = new UserData(index, "firstName" + index, "lastName" + index, "email" + index,
                    role, "gender" + index, "country" + index,
                    "password" + index, null);
            userData.setPosts(Arrays.asList(new UserPost(0L, userData, "post0", new Date(Calendar.getInstance().getTimeInMillis())),
                                            new UserPost(1L, userData, "post1", new Date(Calendar.getInstance().getTimeInMillis()))));
            userDataList.add(userData);
            if (index != loggedInAdminUserIndex) {
                matcherStringsDirectoryAdmin.add(userData.getFirstName());
                matcherStringsDirectoryAdmin.add(userData.getLastName());
                matcherStringsDirectoryAdmin.add(userData.getEmail());
                matcherStringsDirectoryAdmin.add("<form method=\"GET\" action=\"/editUser\">");
                matcherStringsDirectoryAdmin.add("<a class=\"btn btn-light\" href=\"/deleteUser?userId=");
            }
            if (index == loggedInRegularUserIndex) {
                matcherRegularUserSelf.add(Matchers.containsString(userData.getFirstName()));
                matcherRegularUserSelf.add(Matchers.containsString(userData.getLastName()));
                matcherRegularUserSelf.add(Matchers.containsString(userData.getEmail()));
                matcherRegularUserSelf.add(Matchers.containsString(userData.getGender()));
                matcherRegularUserSelf.add(Matchers.containsString(userData.getCountry()));
                userData.getPosts().stream().forEach(post -> {
                    matcherRegularUserSelf.add(Matchers.containsString(post.getPublished().toString()));
                    matcherRegularUserSelf.add(Matchers.containsString(post.getPost()));
                });
            }
        });

        userDataPage = new PageImpl<>(userDataList);
    }


    @WithMockUser
    @Test
    public void directoryTestAdmin() throws Exception {
        Mockito.when(userDataService.getUsersPage(0, 20)).thenReturn(userDataPage);

        this.mockMvc.perform(get("/directory")
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.stringContainsInOrder(matcherStringsDirectoryAdmin)));
    }

    @WithMockUser
    @Test
    public void directoryTestRegular() throws Exception {
        Mockito.when(userDataService.getUsersPage(0, 20)).thenReturn(userDataPage);

        this.mockMvc.perform(get("/directory")
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .string(Matchers.not(Matchers.anyOf(Matchers.containsString("<a class=\"btn btn-light\" href=\"/deleteUser?userId="),
                                                            Matchers.containsString("<form method=\"GET\" action=\"/editUser\">")))));
    }

    @WithMockUser
    @Test
    public void userTestRegularSelfGet() throws Exception {
        this.mockMvc.perform(get("/user")
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.allOf(matcherRegularUserSelf.stream().collect(Collectors.toList()))));
    }

    @WithMockUser
    @Test
    public void userTestRegularOtherGet() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;
        Mockito.when(userDataService.getUser(otherUserId)).thenReturn(userDataList.get(otherUserId.intValue()));

        this.mockMvc.perform(get("/user").param("userId", otherUserId.toString())
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("deletePost"))));
    }

    @WithMockUser
    @Test
    public void userTestAdminOtherGet() throws Exception {
        Long otherUserId = loggedInAdminUserIndex + 1L;
        Mockito.when(userDataService.getUser(otherUserId)).thenReturn(userDataList.get(otherUserId.intValue()));

        this.mockMvc.perform(get("/user").param("userId", otherUserId.toString())
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("deletePost")));
    }

    @WithMockUser
    @Test
    public void userTestRegularSelfPost() throws Exception {
        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", userDataList.get(loggedInRegularUserIndex))
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.allOf(matcherRegularUserSelf.stream().collect(Collectors.toList()))));
    }

    @WithMockUser
    @Test
    public void userTestRegularOtherPost() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;

        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", userDataList.get(otherUserId.intValue()))
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString("deletePost"))));
    }

    @WithMockUser
    @Test
    public void userTestAdminOtherPost() throws Exception {
        Long otherUserId = loggedInAdminUserIndex + 1L;

        this.mockMvc.perform(post("/user").with(csrf()).flashAttr("user", userDataList.get(otherUserId.intValue()))
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("deletePost")));
    }

    @WithMockUser(roles = Roles.REGULAR)
    @Test
    public void newUserTestRegularGet() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;

        this.mockMvc.perform(get("/newUser").with(csrf()).param("userId", otherUserId.toString())
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(roles = Roles.ADMIN)
    @Test
    public void newUserTestAdminGet() throws Exception {
        this.mockMvc.perform(get("/newUser")
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @WithMockUser(roles = Roles.REGULAR)
    @Test
    public void editUserTestRegularSelfGet() throws Exception {
        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(userDataList.get(loggedInRegularUserIndex).getFirstName());
        matcherRegularUserSelf.add(userDataList.get(loggedInRegularUserIndex).getLastName());
        matcherRegularUserSelf.add(userDataList.get(loggedInRegularUserIndex).getEmail());
        matcherRegularUserSelf.add(userDataList.get(loggedInRegularUserIndex).getGender());
        matcherRegularUserSelf.add(userDataList.get(loggedInRegularUserIndex).getCountry());

        this.mockMvc.perform(get("/editUser")
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.not(Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>")))));
    }

    @WithMockUser(roles = Roles.REGULAR)
    @Test
    public void editUserTestRegularOtherGet() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;

        this.mockMvc.perform(get("/editUser").param("user", userDataList.get(otherUserId.intValue()).toString())
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(roles = Roles.ADMIN)
    @Test
    public void editUserTestAdminOtherGet() throws Exception {
        Long otherUserId = loggedInAdminUserIndex + 1L;
        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getFirstName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getLastName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getEmail());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getGender());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getCountry());

        this.mockMvc.perform(get("/editUser").param("user", userDataList.get(otherUserId.intValue()).toString())
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>"))));
    }

    @WithMockUser(roles = Roles.REGULAR)
    @Test
    public void editUserTestRegularSelfPost() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;
        UserData updatedUser = new UserData((long) loggedInRegularUserIndex, userDataList.get(otherUserId.intValue()).getFirstName(),
                                            userDataList.get(otherUserId.intValue()).getLastName(), userDataList.get(otherUserId.intValue()).getEmail(),
                                            userDataList.get(loggedInRegularUserIndex).getRole(), userDataList.get(otherUserId.intValue()).getGender(),
                                            userDataList.get(otherUserId.intValue()).getCountry(), userDataList.get(otherUserId.intValue()).getPassword(),
                                            userDataList.get(otherUserId.intValue()).getPosts());

        Mockito.when(userDataService.updateUser(userDataList.get(otherUserId.intValue()))).thenReturn(userDataList.get(otherUserId.intValue()));


        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(updatedUser.getFirstName());
        matcherRegularUserSelf.add(updatedUser.getLastName());
        matcherRegularUserSelf.add(updatedUser.getEmail());
        matcherRegularUserSelf.add(updatedUser.getGender());
        matcherRegularUserSelf.add(updatedUser.getCountry());

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", updatedUser)
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.not(Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>")))));
    }

    @WithMockUser(roles = Roles.REGULAR)
    @Test
    public void editUserTestRegularOtherPost() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", userDataList.get(otherUserId.intValue()))
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(roles = Roles.ADMIN)
    @Test
    public void editUserTestAdminOtherPost() throws Exception {
        Long otherUserId = loggedInAdminUserIndex + 1L;

        Mockito.when(userDataService.updateUser(userDataList.get(otherUserId.intValue()))).thenReturn(userDataList.get(otherUserId.intValue()));

        List<String> matcherRegularUserSelf = new ArrayList<>();
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getFirstName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getLastName());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getEmail());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getGender());
        matcherRegularUserSelf.add(userDataList.get(otherUserId.intValue()).getCountry());

        this.mockMvc.perform(post("/editUser").with(csrf()).flashAttr("user", userDataList.get(otherUserId.intValue()))
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.allOf(
                                Matchers.stringContainsInOrder(matcherRegularUserSelf),
                                Matchers.containsString("value=\"regular\" selected=\"selected\""),
                                Matchers.containsString("span class=\"input-group-text\" id=\"role\">Role&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>"))));

    }

    @WithMockUser(roles = Roles.REGULAR)
    @Test
    public void deleteUserTestRegularGet() throws Exception {
        Long otherUserId = loggedInRegularUserIndex + 1L;

        this.mockMvc.perform(get("/deleteUser").with(csrf()).param("userId", otherUserId.toString())
                .sessionAttr("user", userDataList.get(loggedInRegularUserIndex)))
                .andDo(MockMvcResultHandlers.print()).andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @WithMockUser(roles = Roles.ADMIN)
    @Test
    public void deleteUserTestAdminGet() throws Exception {
        Long otherUserId = loggedInAdminUserIndex + 1L;
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
                .sessionAttr("user", userDataList.get(loggedInAdminUserIndex))).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(Matchers.not(Matchers.containsString(email))));
    }
}
