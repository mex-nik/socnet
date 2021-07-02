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

package mx.demo.socnet.kafka;

import lombok.extern.slf4j.Slf4j;
import mx.demo.socnet.data.entity.ChatMessage;
import mx.demo.socnet.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 29.06.2021
 * @project socnet
 */

@Slf4j
@Component
public class ChatKafkaConsumer {

    private final ChatService chatService;

    @Autowired
    public ChatKafkaConsumer(ChatService chatService) {
        this.chatService = chatService;
    }

    @KafkaListener(topics = "${kafka.topic.chat}")
    public void processMessage(ChatMessage content) {
        log.info("Received {} ", content);
        chatService.receive(content);
    }
}
