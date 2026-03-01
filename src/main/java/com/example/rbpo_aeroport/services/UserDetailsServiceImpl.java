package com.example.rbpo_aeroport.services;

import com.example.rbpo_aeroport.models.ApplicationUserDto;
import com.example.rbpo_aeroport.models.enums.ApplicationUserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.rbpo_aeroport.entities.ApplicationUser;
import com.example.rbpo_aeroport.repositories.ApplicationUserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ApplicationUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        return fromApplicationUser(user);
    }

    private UserDetails fromApplicationUser(ApplicationUser user) {
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().getGrantedAuthorities())
                .accountLocked(user.isAccountLocked())
                .accountExpired(user.isAccountExpired())
                .credentialsExpired(user.isCredentialsExpired())
                .disabled(user.isDisabled())
                .build();
    }

    public ApplicationUser registerUser(ApplicationUserDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        ApplicationUser user = ApplicationUser.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(ApplicationUserRole.USER)
                .isAccountExpired(false)
                .isAccountLocked(false)
                .isCredentialsExpired(false)
                .isDisabled(false)
                .build();
        return userRepository.save(user);
    }
}