package com.kaayakarpam.auth.service.impl;

import com.kaayakarpam.auth.service.OAuthService;

import com.kaayakarpam.auth.model.User;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import org.json.JSONObject;


public class GoogleOAuthService implements OAuthService{
        private static final String CLIENT_ID             =  "1051516420711-8aa16329abcqkj7ll6ekv1r566mi31q7.apps.googleusercontent.com";
        private static final String CLIENT_SECRET    =  "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";
        private static final String REDIRECT_URI      =   "http://localhost:8080/kaayakarpam/callback";
        private static final String  TOKEN_URL          =   "https://oauth2.googleapis.com/token";
        private static final String  PEOPLE_API_URL= "https://people.googleapis.com/v1/people/me?personFields=phoneNumbers,names,emailAddresses";
        
        @Override
        public String getAuthorizationUrl(){
                 String scope = "openid profile email https://www.googleapis.com/auth/user.phonenumbers.read";
                return "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + CLIENT_ID +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) +
                    "&access_type=offline";
        }
        
        @Override
        public User handleCallback(String code) throws IOException{
                  String accessToken = exchangeCodeForTokens(code);
                  return fetchUserProfile(accessToken);
        }
        
        private String exchangeCodeForTokens(String code) throws IOException{
                 String postData = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8) +
                      "&client_id=" + CLIENT_ID +
                      "&client_secret=" + CLIENT_SECRET +
                      "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                      "&grant_type=authorization_code";
                
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
                //httpURLConnection.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.getOutputStream().write(postData.getBytes(StandardCharsets.UTF_8));
                
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                StringBuilder tokenResponse = new StringBuilder();
                String line;
                while((line = bufferedReader.readLine())!=null){
                          tokenResponse.append(line);
                }
                bufferedReader.close();
                
                JSONObject tokenJson = new JSONObject(tokenResponse.toString()); 
                String accessToken = tokenJson.getString("access_token");
                
                return accessToken;
        }
        
        private User fetchUserProfile(String accessToken) throws IOException{
               HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(PEOPLE_API_URL).openConnection();
               //httpURLConnection.openConnection();
               httpURLConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
               
               BufferedReader userReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
               StringBuilder userResponse = new StringBuilder();
               String line;
              while ((line = userReader.readLine()) != null){
                     userResponse.append(line);
              }
              userReader.close();
              
              /* JSONObject userJson = new JSONObject(userResponse.toString());
              User user = new User();
              user.setEmail(userJson.getJSONArray("emailAddresses").getJSONObject(0).getString("value"));
              user.setName(userJson.getJSONArray("names").getJSONObject(0).getString("givenName"));
              
               if (userJson.has("phoneNumbers")) {
                  user.setPhone(userJson.getJSONArray("phoneNumbers").getJSONObject(0).getString("value"));
              }*/
              
              JSONObject userJson = new JSONObject(userResponse.toString());
              User user = new User();

              // email
              user.setEmail(
                  userJson.getJSONArray("emailAddresses")
                          .getJSONObject(0)
                          .getString("value")
              );

              // name (try givenName, fallback to displayName, then unstructuredName)
              JSONObject nameObj = userJson.getJSONArray("names").getJSONObject(0);
              String name = nameObj.optString("givenName", "");
              if (name.isEmpty()) {
                  name = nameObj.optString("displayName", "");
              }
              if (name.isEmpty()) {
                  name = nameObj.optString("unstructuredName", "");
              }
              user.setName(name);

              // phone (if available)
              if (userJson.has("phoneNumbers")) {
                  user.setPhone(
                      userJson.getJSONArray("phoneNumbers")
                              .getJSONObject(0)
                              .getString("value")
                  );
              }


             return user;
               
        }
            
}
