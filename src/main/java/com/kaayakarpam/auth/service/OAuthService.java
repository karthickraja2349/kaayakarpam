package com.kaayakarpam.auth.service;

import com.kaayakarpam.auth.model.User;

import java.io.IOException;

public interface OAuthService {
    String getAuthorizationUrl();
    User handleCallback(String code) throws IOException;
}
