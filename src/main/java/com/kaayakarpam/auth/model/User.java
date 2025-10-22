package com.kaayakarpam.auth.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.kaayakarpam.common.security.PasswordEncrypter;

//main/java :javac -d ~/Server/apache-tomcat-10.1.44/webapps/kaayakarpam/WEB-INF/classes com/kaayakarpam/auth/model/User.java
public class User {

    private String userName;
    private String email;
    // For local login
    private String passwordHash;
    private String passwordSalt;
    private Role role;               
    private LocalDateTime createdAt;

    private String phone;

    public User(){
    
    }
    //  signup 
    public User(String userName, String email, String plainPassword, Role role) throws Exception{
        this.userName = userName;
        this.email = email;
        this.passwordSalt = PasswordEncrypter.generateSalt();
        this.passwordHash = PasswordEncrypter.hashPassword(plainPassword, this.passwordSalt.getBytes());
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    //  DB fetch
    public User(String userName, String email, String passwordHash, String passwordSalt, Role role, LocalDateTime createdAt) {
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordSalt = passwordSalt;
        this.role = role;
        this.createdAt = createdAt;
    }
    
    public void setName(String name){
          this.userName = userName;
    }
    
    public String getName() { 
            return userName; 
    }
    
    public void setEmail(String email){
          this.email = email;
    }
    
    public String getEmail() { 
           return email; 
    }
    
    public String getPasswordHash() { 
           return passwordHash; 
    }
    
    public String getPasswordSalt() { 
          return passwordSalt; 
    }
    
    public Role getRole() { 
           return role; 
    }
    
    public LocalDateTime getCreatedAt() { 
           return createdAt; 
    }
    
        public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    //  Validate password 
    public boolean checkPassword(String plainPassword) throws Exception{
        return this.passwordHash.equals(PasswordEncrypter.hashPassword(plainPassword, this.passwordSalt.getBytes()));
    }

    @Override
    public String toString() {
        return "User{" +
                ", username='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone +
                '}';
    }

    //Enum
    public enum Role {
        ADMIN,
        DOCTOR,
        PATIENT
    }
}

