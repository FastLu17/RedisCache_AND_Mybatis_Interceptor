package com.luxf.mybatis.bootmybatisdemo.entity;

import java.io.Serializable;

/**
 * @author 小66
 * @date 2020-06-13 20:28
 **/
public class BaseInfo<I extends Serializable> implements Serializable {
    private I id;

    public I getId() {
        return id;
    }

    public void setId(I id) {
        this.id = id;
    }
}
