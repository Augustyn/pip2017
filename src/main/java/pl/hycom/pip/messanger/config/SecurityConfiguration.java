/*
 *   Copyright 2012-2014 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package pl.hycom.pip.messanger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import pl.hycom.pip.messanger.service.UserService;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_ACTUATOR = "ACTUATOR";
    @Autowired
    private UserService userService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/db-admin/console/**", "/reset/forget/password/**", "/change/password/**", "/send/**", "/save/password/**", "/change/password/token/**").permitAll()
                .antMatchers("/admin/**").hasRole(ROLE_ADMIN)
                .anyRequest().authenticated()

                .and()
                .formLogin().loginPage("/login").failureUrl("/login-error.html").permitAll()

                // TODO: usunąć kiedy zrezygnujemy z consoli do łączenia się z H2
                .and()
                .csrf().ignoringAntMatchers("/db-admin/console/**")

                .and()
                .headers().frameOptions().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder authManagerBuilder) throws Exception {
//        authManagerBuilder.inMemoryAuthentication()
//                .withUser("admin").password("admin").roles(ROLE_ADMIN)
//                .and()
//                .withUser("test").password("test").roles(ROLE_ACTUATOR);
        // todo dodac metode passwordEncoder
        authManagerBuilder.userDetailsService(userService);
    }

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring().antMatchers("/css/**", "/js/**", "/webhook");
    }
}
