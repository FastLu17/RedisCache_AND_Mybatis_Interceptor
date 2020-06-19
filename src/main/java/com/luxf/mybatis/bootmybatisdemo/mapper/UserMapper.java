package com.luxf.mybatis.bootmybatisdemo.mapper;

import com.luxf.mybatis.bootmybatisdemo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * @author luxf
 * @date 2020/2/24 22:53
 */
@Repository
@Mapper
public interface UserMapper extends GenericMapper<Integer> {

    User selectUserById(Map<String, Object> map, Integer id);

    void updateUserById(Map<String, Object> map);

    Map<String, Object> selectMapById(Map<String, Object> map);

    Integer insertUser(User user);
}
