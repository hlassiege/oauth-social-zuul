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
package demo.social;

import demo.repository.AccountRepository;
import demo.security.SecurityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SimpleSignInAdapter implements SignInAdapter {

    private final RequestCache requestCache;
    private RememberMeServices rememberMeServices;
    private AccountRepository accountRepository;

    public SimpleSignInAdapter(RequestCache requestCache, AccountRepository accountRepository, RememberMeServices rememberMeServices) {
        this.requestCache = requestCache;
        this.rememberMeServices = rememberMeServices;
        this.accountRepository = accountRepository;
    }

    @Override
    public String signIn(String localUserId, Connection<?> connection, NativeWebRequest request) {
        SecurityUtils.signin(accountRepository.findOne(localUserId));
        rememberMeServices.loginSuccess(request.getNativeRequest(HttpServletRequest.class),
                request.getNativeResponse(HttpServletResponse.class),
                SecurityContextHolder.getContext().getAuthentication());
        return extractOriginalUrl(request);
    }

    private String extractOriginalUrl(NativeWebRequest request) {
        HttpServletRequest nativeReq = request.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse nativeRes = request.getNativeResponse(HttpServletResponse.class);
        SavedRequest saved = requestCache.getRequest(nativeReq, nativeRes);
        if (saved == null) {
            return null;
        }
        requestCache.removeRequest(nativeReq, nativeRes);
        removeAuthenticationAttributes(nativeReq.getSession(false));
        return saved.getRedirectUrl();
    }

    private void removeAuthenticationAttributes(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }

}
