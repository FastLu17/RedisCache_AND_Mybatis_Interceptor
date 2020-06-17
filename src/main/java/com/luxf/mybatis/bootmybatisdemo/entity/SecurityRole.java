package com.luxf.mybatis.bootmybatisdemo.entity;

import javax.persistence.Table;

/**
 * @author 小66
 * @date 2020-06-17 21:28
 **/
@Table(name = "S_ROLE")
public class SecurityRole {
    private Integer id;
    private String roleName;
    private String comment;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
