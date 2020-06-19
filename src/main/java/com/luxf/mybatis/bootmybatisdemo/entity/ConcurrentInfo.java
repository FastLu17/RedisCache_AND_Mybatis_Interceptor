package com.luxf.mybatis.bootmybatisdemo.entity;

import javax.persistence.Table;

/**
 * 测试并发、
 *
 * @author 小66
 * @date 2020-06-19 17:13
 **/
@Table(name = "CONCURRENT")
public class ConcurrentInfo extends BaseInfo<String> {
    private String id;
    private Integer stock;
    private Integer productId;
    private Integer userId;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ConcurrentInfo{" +
                "id='" + id + '\'' +
                ", stock=" + stock +
                ", productId=" + productId +
                ", userId=" + userId +
                '}';
    }
}
