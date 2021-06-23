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
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.entity.UserPost;
import mx.demo.socnet.data.repository.UserPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

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

    private final UserPostRepository userPostRepository;

    @Autowired
    public UserPostController(UserPostRepository userPostRepository) {
        this.userPostRepository = userPostRepository;
    }

    @PostMapping("/addPost")
    public String addPost(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("newpost") UserPost userPost) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
       if (!userPost.getPost().strip().isEmpty()){
            model.addAttribute("user", loggedInUser);
            loggedInUser.getPosts().add(0, userPost);
            userPostRepository.save(userPost);
        }
        return "user";
    }


    @PostMapping("/deletePost")
    public String deletePost(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("post") UserPost post) {
        post.getUser().getPosts().removeIf(post1 -> (post.getId().longValue() == post1.getId().longValue()));
        model.addAttribute("user", post.getUser());
        userPostRepository.delete(post);
        return "user";
    }
}
