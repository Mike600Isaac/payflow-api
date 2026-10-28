package com.isaacmicheal.payflow.security;

import com.isaacmicheal.payflow.domain.Merchant;
import com.isaacmicheal.payflow.repository.MerchantRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final MerchantRepository merchants;

    public AppUserDetailsService(MerchantRepository merchants) {
        this.merchants = merchants;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Merchant merchant = merchants.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No merchant found for: " + email));
        return User.withUsername(merchant.getEmail())
                .password(merchant.getPasswordHash())
                .authorities(new SimpleGrantedAuthority("ROLE_" + merchant.getRole().name()))
                .build();
    }
}
