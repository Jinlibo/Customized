package org.example.security;


import lombok.Getter;
import lombok.Setter;
import org.example.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomSecurityUser implements UserDetails {
    @Getter
    private final SysUser sysUser;
    //用于存储权限的list
    @Setter
    private List<SimpleGrantedAuthority> authorityList;

    public CustomSecurityUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorityList;
    }

    @Override
    public String getPassword() {
        String myPassword = sysUser.getPassword();
        sysUser.setPassword(null); //擦除我们的密码，防止传到前端
        return myPassword;
    }

    @Override
    public String getUsername() {
        return this.sysUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return sysUser.getEnabled().equals(1);
    }
}
