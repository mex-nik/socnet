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

import lombok.extern.slf4j.Slf4j;
import mx.demo.socnet.data.entity.Roles;
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.entity.UserPost;
import mx.demo.socnet.service.UserDataService;
import mx.demo.socnet.service.UserPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 13.06.2021
 * @project socnet
 */

@Slf4j
@Controller
@RequestMapping
public class UserPostController {

    private final UserPostService userPostService;

    private final UserDataService userDataService;

    @Autowired
    public UserPostController(UserPostService userPostService, UserDataService userDataService) {
        this.userPostService = userPostService;
        this.userDataService = userDataService;
    }

    @PostMapping("/addPost")
    public String addPost(
            Model model,
            @ModelAttribute("newpost") UserPost post) {
        UserData loggedInUser = userDataService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!loggedInUser.equals(post.getUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permissions to add post for another user");
        }
       if (!post.getPost().strip().isEmpty()){
            model.addAttribute("user", loggedInUser);
            loggedInUser.getPosts().add(0, post);
            userPostService.addPost(post);
        }

        return "user";
    }


    @PostMapping("/deletePost")
    public String deletePost(
            Model model,
            @ModelAttribute("post") UserPost post) {
        UserData loggedInUser = userDataService.getUser(SecurityContextHolder.getContext().getAuthentication().getName());
        if (!loggedInUser.getRole().getAuthority().equals(Roles.ADMIN) && !loggedInUser.equals(post.getUser())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permissions to remove post");
        }
        post.getUser().getPosts().removeIf(post1 -> (post.getId().equals(post1.getId())));
        model.addAttribute("user", post.getUser());
        userPostService.removePost(post);
        return "user";
    }
}
