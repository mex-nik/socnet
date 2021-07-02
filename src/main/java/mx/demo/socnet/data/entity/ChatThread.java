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

package mx.demo.socnet.data.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 30.06.2021
 * @project socnet
 */

public class ChatThread {
    private final Key chatKey;
    private List<ChatMessage> chat = new ArrayList<>();

    public ChatThread(Key chatKey) {
        this.chatKey = chatKey;
    }

    public void addMessage(ChatMessage chatMessage){
        if (isRightThread(chatKey.getFromUserId(),chatMessage.getToUserId())) {
            chat.add(chatMessage);
        }
    }

    private boolean isRightThread(Long fromId, Long toId) {
        return chatKey.equals(new Key(fromId, toId));
    }

    public List<ChatMessage> getChat() {
        return chat.stream().sorted().collect(Collectors.toList());
    }

    @Data
    @Slf4j
    public static class Key {
        @NonNull
        private Long fromUserId;
        @NonNull
        private Long toUserId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            log.info("equals for f={}, t={}, and f={}, t={}", fromUserId, toUserId, key.fromUserId, key.toUserId);
            return Arrays.asList(fromUserId, toUserId, key.fromUserId, key.toUserId).stream().distinct().count() == 2;
        }

        @Override
        public int hashCode() {
            List<Long> sorted = Arrays.asList(fromUserId, toUserId).stream().sorted().collect(Collectors.toList());
            log.info("hash for f={}, t={} is {}", fromUserId, toUserId, Objects.hash(sorted.get(0), sorted.get(1)));
            return Objects.hash(sorted.get(0), sorted.get(1));
        }
    }
}
