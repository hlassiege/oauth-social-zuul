/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.security;

import demo.model.SimpleAccount;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;


public class SecurityUtils {

    public static GrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
    public static GrantedAuthority ROLE_ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    /**
     * Programmatically signs in the user with the given the user ID.
     */
    static void signin(AccountWrapper userDetailsPrincipal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetailsPrincipal,
                        null,
                        userDetailsPrincipal.getAuthorities()));
    }

    public static void signin(SimpleAccount account,  GrantedAuthority... roles) {
        final AccountWrapper userDetailsPrincipal = new AccountWrapper(account);
        userDetailsPrincipal.getAuthorities().addAll(Arrays.asList(roles));
        signin(userDetailsPrincipal);
    }

    public static String connectedAccountId() {
        if(!isConnected()) {
            return null;
        }
        return connectedAccount().getId();
    }

    public static SimpleAccount connectedAccount() {
        return ((AccountWrapper) currentAuthentication().getPrincipal()).getAccount();
    }

    public static Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static boolean isConnected() {
        return currentAuthentication() != null
                && currentAuthentication().getAuthorities().contains(ROLE_USER);
    }

    public static boolean isAdmin() {
        return currentAuthentication() != null
                && currentAuthentication().getAuthorities().contains(ROLE_ADMIN);
    }

}
