package com.luxf.mybatis.bootmybatisdemo.service;

import com.luxf.mybatis.bootmybatisdemo.entity.StudentInfo;
import org.springframework.stereotype.Service;

/**
 * @author 小66
 * @date 2020-06-16 14:56
 **/
@Service
public class GenericClassTypeService implements BaseService<StudentInfo, Integer> {
    @Override
    public StudentInfo findInfoById(Integer id) {
        return null;
    }
}
