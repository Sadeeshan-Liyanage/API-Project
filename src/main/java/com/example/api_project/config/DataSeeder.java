package com.example.api_project.config;


import com.example.api_project.entity.Branch;
import com.example.api_project.entity.Role;
import com.example.api_project.entity.User;
import com.example.api_project.repository.BranchRepository;
import com.example.api_project.repository.RoleRepository;
import com.example.api_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;



    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_ADMIN").description("Full system access").build()));
        roleRepository.findByName("ROLE_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_MANAGER").description("Branch / operations management").build()));
        roleRepository.findByName("ROLE_CASHIER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_CASHIER").description("Point-of-sale, read-only catalog").build()));

        Branch mainBranch = branchRepository.count() == 0
                ? branchRepository.save(Branch.builder()
                        .name("Main Branch").address("Head Office").phone("011-0000000").active(true).build())
                : branchRepository.findAll().get(0);

        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .email("admin@grocery.local")
                    .fullName("System Administrator")
                    .enabled(true)
                    .role(adminRole)
                    .branch(mainBranch)
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin user -> username: admin / password: Admin@123");
        }
    }


}



