package com.pharmacy.security;

import com.pharmacy.entity.UserAccount;
import com.pharmacy.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        return new AppUserPrincipal(
                user.getId(),
                user.getPharmacy().getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                Boolean.TRUE.equals(user.getActive())
        );
    }
}
