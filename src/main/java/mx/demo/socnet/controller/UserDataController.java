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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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

    @GetMapping("/")
    public String getUser(HttpSession httpSession, Model model) {
        return "home";
    }

    @GetMapping("/directory")
    public String listUsers(
            HttpSession httpSession,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {

        Page<UserData> userData = userDataService.getUsersPage(page.orElse(0), size.orElse(pageSize));
        model.addAttribute("userdatas", userData);

        model.addAttribute("pageNumbers", pageNumbers(userData.getTotalPages()));
        return "directory";
    }

    @GetMapping("/user")
    public String userDataGet(
            HttpSession httpSession,
            Model model,
            @RequestParam("userId") Optional<Long> userId) {
        UserData user = (UserData) httpSession.getAttribute("user");
        if (userId.isPresent()) {
            user = userDataService.getUser(userId.get());
        }
        model.addAttribute("user", user);
        return "user";
    }

    @PostMapping("/user")
    public String userDataPost(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("user") UserData user) {
        return "user";
    }

    @GetMapping("/newUser")
    public String newUserDataGet(
            HttpSession httpSession,
            Model model) {

        model.addAttribute("user", new UserData());

        return "edituser";
    }

    @GetMapping("/editUser")
    public String editUserDataGet(
            HttpSession httpSession,
            Model model,
            @RequestParam("user") Optional<UserData> user) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        UserData userData = user.orElse(loggedInUser);

        if (!loggedInUser.getRole().getAuthority().equals(Roles.ADMIN) && loggedInUser.getId().longValue() != userData.getId().longValue()) {
            throw new AccessDeniedException("Not permissions to edit user");
        }

        model.addAttribute("user", userData);

        return "edituser";
    }

    @PostMapping("/editUser")
    public String editUserDataPost(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("user") UserData user) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        if (!loggedInUser.getRole().getAuthority().equals(Roles.ADMIN) && loggedInUser.getId().longValue() != user.getId().longValue()) {
            throw new AccessDeniedException("Not permissions to edit user");
        }
        UserData updatedUser = userDataService.updateUser(user);
        if (updatedUser == null) {
            model.addAttribute("error", true);
        } else {
            model.addAttribute("success", true);

            if (loggedInUser.getId().longValue() == updatedUser.getId().longValue()) {
                httpSession.setAttribute("user", updatedUser);
            }
            model.addAttribute("user", updatedUser);
        }
        return "edituser";
    }

    @GetMapping("deleteUser")
    public String deleteUser(
            HttpSession httpSession,
            Model model,
            @RequestParam(value = "userId") Long userId,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size) {

        userDataService.deleteUserById(userId);

        Page<UserData> userData = userDataService.getUsersPage(page.orElse(0), size.orElse(pageSize));
        model.addAttribute("userdatas", userData);

        model.addAttribute("pageNumbers", pageNumbers(userData.getTotalPages()));

        return "directory";
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
