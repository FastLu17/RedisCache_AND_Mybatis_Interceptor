package com.luxf.mybatis.bootmybatisdemo.security;

import com.luxf.mybatis.bootmybatisdemo.entity.SecurityRole;
import com.luxf.mybatis.bootmybatisdemo.entity.SecurityUser;
import com.luxf.mybatis.bootmybatisdemo.mapper.SecurityUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 小66
 * @date 2020-06-17 21:17
 **/
@Service
public class UserRoleService {
    private final SecurityUserMapper userMapper;

    @Autowired
    public UserRoleService(SecurityUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Cacheable(value = "SECURITY_USER", key = "#result.id")
    public SecurityUser getUserByUserName(String userName) {
        return userMapper.findUserByUserName(userName);
    }

    /**
     * 缓存用户名具有的所有角色、
     *
     * @param userName 用户名
     * @return roleList
     */
    @Cacheable(value = "SECURITY_ROLE", key = "#userName")
    public List<SecurityRole> getRolesByUserName(String userName) {
        return userMapper.findRolesByUserName(userName);

    }
}
