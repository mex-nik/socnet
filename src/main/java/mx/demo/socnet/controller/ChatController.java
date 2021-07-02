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
import mx.demo.socnet.data.entity.ChatMessage;
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.service.ChatService;
import mx.demo.socnet.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 29.06.2021
 * @project socnet
 */

@Slf4j
@Controller
@RequestMapping
public class ChatController {

    private final ChatService chatService;
    private final UserDataService userDataService;

    @Autowired
    public ChatController(ChatService chatService, UserDataService userDataService) {
        this.chatService = chatService;
        this.userDataService = userDataService;
    }

    @GetMapping("/chat")
    public String chatWithUser(
            HttpSession httpSession,
            Model model,
            @RequestParam(value = "userId") Long userId,
            @ModelAttribute("message") ChatMessage message,
            @ModelAttribute("receiver") UserData receiver) {
        UserData user = (UserData) httpSession.getAttribute("user");
        receiver = userDataService.getUser(userId);
        List<ChatMessage> chat = chatService.getThread(user.getId(), userId);
        message.setFromUserId(user.getId());
        message.setToUserId(userId);
        model.addAttribute("chatThread", chat);
        model.addAttribute("receiver", receiver);

        return "chat";
    }

    @PostMapping("/sendMessage")
    public String sendMessage(
            HttpSession httpSession,
            Model model,
            @ModelAttribute("message") ChatMessage message,
            @ModelAttribute("receiver") UserData receiver,
            RedirectAttributes redirectAttributes) {
        receiver = userDataService.getUser(message.getToUserId());
        chatService.send(message);
        redirectAttributes.addAttribute("userId", message.getToUserId());
        model.addAttribute("receiver", receiver);
        return "redirect:chat";
    }
}
