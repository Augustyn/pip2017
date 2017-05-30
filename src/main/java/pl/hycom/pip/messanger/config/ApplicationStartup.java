/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.hycom.pip.messanger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.hycom.pip.messanger.repository.model.Role;
import pl.hycom.pip.messanger.repository.model.User;
import pl.hycom.pip.messanger.service.RoleService;
import pl.hycom.pip.messanger.service.UserService;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by Maciek on 2017-05-27.
 */
@Component
@Slf4j
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    private final String ROLE_ADMIN = Role.RoleName.ROLE_ADMIN.name();
    private final String ROLE_USER = Role.RoleName.ROLE_USER.name();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        log.info("onApplicationEvent method invoked");

        initializeRole(ROLE_ADMIN);
        initializeRole(ROLE_USER);

        if (userService.findAllUsers().isEmpty()) {
            initializeAdminUser();
        }
    }

    private void initializeAdminUser() {
        log.info("initializeAdminUser method invoked");

        User user = new User("admin", "admin", "admin@example.com", "admin", "+48923456783");
        Optional<Role> role = roleService.findRoleByName(ROLE_ADMIN);
        if (role.isPresent()) {
            user.setRoles(Collections.singleton(role.get()));
            userService.addUser(user);
        } else {
            log.warn("ApplicationStartup.initializeAdminUser() - Initializing admin failed!!");
        }
    }

    private void initializeRole(final String roleName) {
        log.info("initializeRole method invoked for role " + roleName);

        Optional<Role> role = roleService.findRoleByName(roleName);
        if (!role.isPresent()) {
            Role newRole = new Role(roleName);
            roleService.addRole(newRole);
        }
    }
}
