package com.dbs.tpc_benchmark.config;

import com.dbs.tpc_benchmark.typings.entity.User;
import com.dbs.tpc_benchmark.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Component
public class AdminInit {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.default.password:rooo123t}")
    private String AdminPassword;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeAdminUser() {
        if (userRepository.findByName("root") == null) {
            User adminUser = new User();
            adminUser.setName("root");
            adminUser.setPassword(passwordEncoder.encode(AdminPassword));
            adminUser.setRole("ADMIN");
            adminUser.setStatus("APPROVED");
            userRepository.save(adminUser);
            System.out.println("Administator root created");
        }
    }
}
