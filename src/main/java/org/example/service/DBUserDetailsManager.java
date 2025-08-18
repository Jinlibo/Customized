package org.example.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.entity.SysUser;
import org.example.mapper.SysUserMapper;
import org.example.security.CustomSecurityUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class DBUserDetailsManager implements UserDetailsManager, UserDetailsPasswordService {

    @Resource
    private SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username);
        SysUser sysUser = userMapper.selectOne(queryWrapper);
        if (sysUser == null) {
            throw new UsernameNotFoundException(username);
        } else {
            // 返回自定义的UserDetails实现
            CustomSecurityUser customSecurityUser = new CustomSecurityUser(sysUser);
            // 这里应该加载用户的权限信息
            List<SimpleGrantedAuthority> authorities = loadUserAuthorities(sysUser.getUserId());
            customSecurityUser.setAuthorityList(authorities);
            return customSecurityUser;
        }
    }

    // 添加权限加载方法
    private List<SimpleGrantedAuthority> loadUserAuthorities(Integer userId) {
        // TODO: 从数据库加载用户权限
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        return authorities;
    }


    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        return null;
    }

    @Override
    public void createUser(UserDetails user) {

    }

    @Override
    public void updateUser(UserDetails user) {

    }

    @Override
    public void deleteUser(String username) {

    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {

    }

    @Override
    public boolean userExists(String username) {
        return false;
    }
}