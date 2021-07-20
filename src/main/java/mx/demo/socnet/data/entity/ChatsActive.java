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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 01.07.2021
 * @project socnet
 */

public class ChatsActive {
    private final UserData owner;
    private List<ChatThread.Key> chatThreads = new ArrayList<>();

    public ChatsActive(UserData owner) {
        this.owner = owner;
    }

    public void addThread(ChatThread.Key chatKey){
        chatThreads.add(chatKey);
    }

    public List<Long> getChatPartnersIds() {
        return chatThreads.stream().map(chatThread -> {
            Long fromId = chatThread.getFromUserId();
            if (fromId.equals(owner.getId())) {
                fromId = chatThread.getToUserId();
            }
            return fromId;
        }).distinct().sorted().collect(Collectors.toList());
    }
}
