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
import mx.demo.socnet.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 06.06.2021
 * @project socnet
 */

@Slf4j
@Controller
@RequestMapping
public class UserDataController {

    @Value("${defaults.page-size}")
    private int pageSize;

    private final UserDataService userDataService;

    @Autowired
    public UserDataController(UserDataService userDataService) {
        this.userDataService = userDataService;
    }

    @GetMapping("/directory")
    public String listUsers(
            HttpSession httpSession,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        model.addAttribute("userLogin", loggedInUser);

        Page<UserData> userData = userDataService.getUsersPage(page.orElse(0), size.orElse(pageSize));
        model.addAttribute("userdatas", userData);

        model.addAttribute("pageNumbers", pageNumbers(userData.getTotalPages()));
        return "directory";
    }

    @PostMapping("/user")
    public String userDataPost(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("user") UserData user) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        model.addAttribute("userLogin", loggedInUser);
        return "user";
    }

    @GetMapping("/user")
    public String userDataGet(
            HttpSession httpSession,
            Model model,
            @RequestParam("userId") Optional<Long> userId) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        model.addAttribute("userLogin", loggedInUser);

        UserData user;
        if (userId.isPresent()) {
            user = userDataService.getUser(userId.get());
        } else {
            user = loggedInUser;
        }
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/edituser")
    public String editUserDataGet(
            HttpSession httpSession,
            Model model,
            @RequestParam("user") Optional<UserData> user,
            @RequestParam(value = "new", required = false, defaultValue = "false") boolean isNew) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        UserData userData = user.orElse(loggedInUser);

        if (isNew) {
            model.addAttribute("user", new UserData());
        } else {
            model.addAttribute("user", userData);
        }
        return "edituser";
    }

    @PostMapping("/edituser")
    public String editUserDataPost(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("user") UserData user) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        if (loggedInUser.getId().longValue() == user.getId().longValue()) {
            user.setPosts(loggedInUser.getPosts());
        } else {
            user.setPosts(new ArrayList<>());
        }

        UserData updatedUser = userDataService.updateUser(user);
        if (updatedUser == null) {
            model.addAttribute("error", true);
            model.addAttribute("success", false);
            return "edituser";
        }

        model.addAttribute("success", true);
        model.addAttribute("error", false);

        if (loggedInUser.getId().longValue() == updatedUser.getId().longValue()) {
            httpSession.setAttribute("user", updatedUser);
        }
        model.addAttribute("user", updatedUser);
        return "edituser";
    }

    @GetMapping("deleteUser")
    public String deleteUser(
            HttpSession httpSession,
            Model model,
            @RequestParam(value = "userId") Long userId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        if (loggedInUser.getRole().getAuthority() == Roles.ADMIN) {
            userDataService.deleteUserById(userId);
        }

        Page<UserData> userData = userDataService.getUsersPage(page.orElse(0), size.orElse(pageSize));
        model.addAttribute("userdatas", userData);

        model.addAttribute("pageNumbers", pageNumbers(userData.getTotalPages()));

        return "directory";
    }


    @GetMapping("/")
    public String getUser(HttpSession httpSession, Model model) {
        return "home";
    }

    private List<Integer> pageNumbers(int totalPages) {
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            return pageNumbers;
        }
        return null;
    }
}
