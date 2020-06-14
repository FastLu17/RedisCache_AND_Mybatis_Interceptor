package com.luxf.mybatis.bootmybatisdemo.entity;

/**
 * @author 小66
 * @date 2020-06-13 20:30
 **/
public class StudentInfo extends BaseInfo<Integer> {
    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}
