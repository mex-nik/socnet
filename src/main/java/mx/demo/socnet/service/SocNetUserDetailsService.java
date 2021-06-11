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
import mx.demo.socnet.data.entity.Roles;
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.repository.UserDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.security.core.userdetails.User.withUsername;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 09.06.2021
 * @project socnet
 */

@Slf4j
@Service
public class SocNetUserDetailsService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    @Autowired
    public SocNetUserDetailsService(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        List<UserData> userDatas = userDataRepository.findByEmail(email);
        if (userDatas.isEmpty()) {
            new UsernameNotFoundException(String.format("User with email %s does not exist", email));
        } else if (userDatas.size() > 1){
            new UsernameNotFoundException(String.format("Multiple users with email %s", email));
        }

        return withUsername(userDatas.get(0).getEmail())
                .password(userDatas.get(0).getPassword())
                .passwordEncoder(pass -> "{noop}" + pass)
                .authorities(userDatas.get(0).getRole())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
