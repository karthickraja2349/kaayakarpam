package com.kaayakarpam.common.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.nio.charset.StandardCharsets;

public class PasswordEncrypter{
           private static final int ITERATIONS = 65536;
           private static final int KEY_LENGTH = 256;
           private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
           
           public static String hashPassword(String password, byte salts[])throws NoSuchAlgorithmException, InvalidKeySpecException{
                 PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salts, ITERATIONS, KEY_LENGTH);
                 SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM);
                 byte[] hashed = secretKeyFactory.generateSecret(spec).getEncoded();
                 return Base64.getEncoder().encodeToString(hashed);
           }
           
           public static byte[] generateSalt(){
                  SecureRandom random = new SecureRandom();
                  byte[] salt = new byte[16];
                  random.nextBytes(salt);
                  return new String(salt,  StandardCharsets.UTF_8).getBytes();
           }
           
            public static String encodeSalt(byte[] salt) {
                   return Base64.getEncoder().encodeToString(salt);
            }

            public static byte[] decodeSalt(String saltStr) {
                 return Base64.getDecoder().decode(saltStr);
            }
           
}
