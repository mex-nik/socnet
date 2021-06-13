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
import mx.demo.socnet.data.entity.UserData;
import mx.demo.socnet.data.repository.UserDataRepository;
import org.apache.el.stream.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Mladen Nikolic <mladen.nikolic.mex@gmail.com>
 * https://www.linkedin.com/in/mladen-nikolic-mex/
 * @created 06.06.2021
 * @project socnet
 */

@Slf4j
@Service
public class UserDataService {

    private final UserDataRepository userDataRepository;

    @Autowired
    public UserDataService(UserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }

    public List<UserData> getAllUsers() {
        return (List<UserData>) userDataRepository.findAll();
    }

    public Page<UserData> getUsersPage(int page, int size) {
        return userDataRepository.findAll(PageRequest.of(page, size));
    }

    public UserData getUser(long id) {
        return userDataRepository.findById(id).orElse(null);
    }

    public List<UserData> getUser(String email) {
        return userDataRepository.findByEmail(email);
    }

    public void updateUser(UserData userData) {
        userDataRepository.save(userData);
    }

}
