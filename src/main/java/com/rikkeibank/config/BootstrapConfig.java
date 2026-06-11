package com.rikkeibank.config;

import com.rikkeibank.model.*;
import com.rikkeibank.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapConfig {
    @Bean CommandLineRunner admin(UserRepository users,RoleRepository roles,PasswordEncoder encoder){
        return args->{if(users.findByUsername("admin").isEmpty()){User u=new User();u.setUsername("admin");u.setPassword(encoder.encode("Admin@123"));u.setEmail("admin@rikkeibank.local");u.setPhoneNumber("0000000000");u.setRole(roles.findByName(RoleName.ADMIN).orElseThrow());users.save(u);}};
    }
}
