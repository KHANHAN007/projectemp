package com.rikkeibank.config;

import com.rikkeibank.domain.RoleName;
import com.rikkeibank.domain.User;
import com.rikkeibank.repository.RoleRepository;
import com.rikkeibank.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapConfig {
    @Bean CommandLineRunner admin(UserRepository users,RoleRepository roles,PasswordEncoder encoder){
        return args->{if(users.findByUsername("admin").isEmpty()){User u=new User();u.setUsername("admin");u.setPassword(encoder.encode("Admin@123"));u.setEmail("admin@rikkeibank.local");u.setPhoneNumber("0000000000");u.setRole(roles.findByName(RoleName.ADMIN).orElseThrow());users.save(u);}};
    }
}
