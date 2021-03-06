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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.demo.socnet.util.BooleanToRolesConverter;
import mx.demo.socnet.util.RolesConverter;
import mx.demo.socnet.util.RolesToBooleanConverter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 04.06.2021
 * @project socnet
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
@JsonSerialize
public class UserData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = 0L;
    private String firstName;
    private String lastName;
    private String email;
    @JsonSerialize(converter = RolesToBooleanConverter.class)
    @JsonDeserialize(converter = BooleanToRolesConverter.class)
    @Convert(converter = RolesConverter.class)
    @Column(name = "admin")
    private Roles role = new Roles(Roles.REGULAR);
    private String gender;
    private String country;
    private String password;
    @JsonManagedReference
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @OrderBy("published DESC")
    private List<UserPost> posts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserData)) return false;
        UserData userData = (UserData) o;
        return getId().equals(userData.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return super.toString();
    }
}
