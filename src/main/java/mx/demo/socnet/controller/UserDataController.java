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

import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
        model.addAttribute("user", loggedInUser);

        Page<UserData> userData = userDataService.getUsersPage(page.orElse(0), size.orElse(pageSize));
        model.addAttribute("userdatas", userData);

        model.addAttribute("pageNumbers", pageNumbers(userData.getTotalPages()));
        return "directory";
    }

    @PostMapping("/user")
    public String userDataPost(
            Model model,
            @ModelAttribute("user") UserData user) {
        return "user";
    }

    @GetMapping("/user")
    public String userDataGet(
            HttpSession httpSession,
            Model model,
            @RequestParam("userId") Optional<Long> userId) {
        UserData user;
        if (userId.isPresent()) {
            user = userDataService.getUser(userId.get());
        } else {
            user = (UserData) httpSession.getAttribute("user");
        }
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/")
    public String getUser(HttpSession httpSession, Model model) {
        UserData loggedInUser = (UserData) httpSession.getAttribute("user");
        model.addAttribute("userLogin", loggedInUser);
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
