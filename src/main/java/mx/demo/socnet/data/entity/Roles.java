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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 09.06.2021
 * @project socnet
 */
@Value
@JsonSerialize
public class Roles implements GrantedAuthority {

    public final static String ADMIN = "admin";
    public final static String REGULAR = "regular";

    private final String authority;

    @Override
    public String getAuthority() {
        return authority;
    }
}
