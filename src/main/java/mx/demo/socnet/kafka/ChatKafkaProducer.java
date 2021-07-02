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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 29.06.2021
 * @project socnet
 */

@Slf4j
@Component
public class ChatKafkaProducer {

    @Value("${kafka.topic.chat}")
    private String topic;

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    public ChatKafkaProducer(KafkaTemplate<String, ChatMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(ChatMessage data) {
        log.info("Sending message:{}", data);
        kafkaTemplate.send(topic, data.getFromUserId() + "", data);
    }
}
