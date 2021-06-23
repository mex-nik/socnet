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

package mx.demo.socnet;

import mx.demo.socnet.controller.UserDataController;
import mx.demo.socnet.controller.UserPostController;
import mx.demo.socnet.data.repository.UserDataRepository;
import mx.demo.socnet.data.repository.UserPostRepository;
import mx.demo.socnet.service.SocNetUserDetailsService;
import mx.demo.socnet.service.UserDataService;
import mx.demo.socnet.service.UserPostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SocnetApplicationTests {

	@Autowired
	private UserDataController userDataController;

	@Autowired
	private UserPostController userPostController;

	@Autowired
	private UserDataService userDataService;

	@Autowired
	private UserPostService userPostService;

	@Autowired
	private SocNetUserDetailsService socNetUserDetailsService;

	@Autowired
	private UserDataRepository userDataRepository;

	@Autowired
	private UserPostRepository userPostRepository;



	@Test
	void contextLoads() {
		Assertions.assertNotNull(userDataController);
		Assertions.assertNotNull(userPostController);
		Assertions.assertNotNull(userDataService);
		Assertions.assertNotNull(userPostService);
		Assertions.assertNotNull(socNetUserDetailsService);
		Assertions.assertNotNull(userDataRepository);
		Assertions.assertNotNull(userPostRepository);
	}

}
