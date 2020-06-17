package com.luxf.mybatis.bootmybatisdemo.mapper;

import com.luxf.mybatis.bootmybatisdemo.entity.SecurityRole;
import com.luxf.mybatis.bootmybatisdemo.entity.SecurityUser;
import com.luxf.mybatis.bootmybatisdemo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @author luxf
 * @date 2020/2/24 22:53
 */
@Repository
@Mapper
public interface SecurityUserMapper {

    SecurityUser findUserByUserName(String userName);

    List<SecurityRole> findRolesByUserName(String userName);
}
