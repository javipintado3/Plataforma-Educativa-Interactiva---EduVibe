package com.example.EduvibeBackend.utility;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "12345678";
        for (int i = 0; i < 5; i++) {
            String encodedPassword = encoder.encode(rawPassword);
            System.out.println(encodedPassword);
        }
    }
}
