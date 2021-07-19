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

import lombok.extern.slf4j.Slf4j;
import mx.demo.socnet.data.entity.ChatMailbox;
import mx.demo.socnet.data.entity.ChatMessage;
import mx.demo.socnet.data.entity.ChatThread;
import mx.demo.socnet.data.repository.UserDataRepository;
import mx.demo.socnet.kafka.ChatKafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 29.06.2021
 * @project socnet
 */

@Slf4j
@Service
public class ChatService {

    private final ChatKafkaProducer producer;
    private final UserDataRepository userDataRepository;
    private final Map<ChatThread.Key, ChatThread> chatThreadHashMap = new HashMap<>();
    private final Map<Long, ChatMailbox> chatMailboxHashMap = new HashMap<>();

    @Autowired
    public ChatService(ChatKafkaProducer producer, UserDataRepository userDataRepository) {
        this.producer = producer;
        this.userDataRepository = userDataRepository;
    }

    public void send(ChatMessage message) {
        producer.send(message);
    }

    public void receive(ChatMessage message){
        ChatThread.Key key = new ChatThread.Key(message.getFromUserId(), message.getToUserId());
        ChatThread currentThread = chatThreadHashMap.get(key);
        ChatMailbox currentMailbox = chatMailboxHashMap.get(message.getToUserId());
        if (currentThread == null) {
            currentThread = new ChatThread(key);
            chatThreadHashMap.put(key, currentThread);
        }
        if (currentMailbox == null) {
            currentMailbox = new ChatMailbox(userDataRepository.findById(message.getToUserId()).orElseThrow());
            chatMailboxHashMap.put(message.getToUserId(), currentMailbox);
        }
        currentThread.addMessage(message);
        currentMailbox.addMessage(message);
    }

    public List<ChatMessage> getThread(Long userId1, Long userId2, boolean orderReverse){
        return getThread(userId1, userId2, new Date(0), orderReverse);
    }

    public List<ChatMessage> getThread(Long userId1, Long userId2, Date after, boolean orderReverse){
        ChatThread.Key key = new ChatThread.Key(userId1, userId2);
        ChatThread currentThread = chatThreadHashMap.get(key);
        if (currentThread == null) {
            currentThread = new ChatThread(key);
            chatThreadHashMap.put(key, currentThread);
        }
        if (orderReverse) {
            return currentThread.getChat(after).stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        } else {
            return currentThread.getChat(after);
        }
    }
}