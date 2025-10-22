package com.kaayakarpam.auth.service;

import java.util.Map;
import java.util.HashMap;

import com.kaayakarpam.auth.service.impl.GoogleOAuthService;

public class OAuthServiceFactory{
       
       private static final Map<String, OAuthService> outhProvidiers = new HashMap();
       
       static{
             outhProvidiers.put("google", new GoogleOAuthService());
             //outhProvidiers.put("facebook", new FaceBookOAuthService());
             //outhProvidiers.put("github", new GithubOAuthService());
       }
       
       public static OAuthService getService(String provider){
                 return outhProvidiers.get(provider.toLowerCase());
       }
}
