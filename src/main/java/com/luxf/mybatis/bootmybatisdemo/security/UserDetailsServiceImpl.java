package com.luxf.mybatis.bootmybatisdemo.security;

import com.luxf.mybatis.bootmybatisdemo.entity.SecurityRole;
import com.luxf.mybatis.bootmybatisdemo.entity.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring Security最常用的自定义认证服务、
 * 自定义用户凭证服务,实现Spring Security提供的接口{@link UserDetailsService}
 *
 * @author 小66
 * @date 2020-06-17 21:15
 **/
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRoleService userRoleService;

    @Autowired
    public UserDetailsServiceImpl(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * 获取UserDetails对象、具有详细的认证信息
     *
     * @param userName 用户名、
     * @return {@link UserDetails}
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        SecurityUser user = userRoleService.getUserByUserName(userName);
        List<SecurityRole> roleList = userRoleService.getRolesByUserName(userName);
        // GrantedAuthority 有3个实现类、
        List<GrantedAuthority> authorityList = roleList.stream().map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
        return new User(user.getUserName(), user.getPsw(), authorityList);

    }
}
