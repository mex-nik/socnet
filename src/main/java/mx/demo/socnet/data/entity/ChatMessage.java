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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 29.06.2021
 * @project socnet
 */

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor(onConstructor = @__({@JsonCreator}))
@JsonSerialize
public class ChatMessage implements Comparable<ChatMessage> {

    private Long fromUserId;
    private Long toUserId;

    private String content;
    private Date timeStamp = new Date(Calendar.getInstance().getTimeInMillis());

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage that = (ChatMessage) o;
        return getFromUserId().equals(that.getFromUserId()) && getToUserId().equals(that.getToUserId()) && getTimeStamp().equals(that.getTimeStamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFromUserId(), getToUserId(), getTimeStamp());
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "from=" + fromUserId +
                ", to=" + toUserId +
                ", content='" + content + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }

    @Override
    public int compareTo(@NotNull ChatMessage chatMessage) {
        return timeStamp.compareTo(chatMessage.timeStamp);
    }
}
