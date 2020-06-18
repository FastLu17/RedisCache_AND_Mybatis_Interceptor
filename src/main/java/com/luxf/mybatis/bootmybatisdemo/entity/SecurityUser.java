package com.luxf.mybatis.bootmybatisdemo.entity;

import javax.persistence.Table;

/**
 * 正常情况 就是USER表、之前的USER表字段不可用,就新建一张
 *
 * @author 小66
 * @date 2020-06-17 21:19
 **/
@Table(name = "S_USER")
public class SecurityUser extends BaseInfo<Integer> {
    private Integer id;
    private String userName;
    private String psw;
    private String available;
    private String comment;


    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "SecurityUser{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", psw='" + psw + '\'' +
                ", available='" + available + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
