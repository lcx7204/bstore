package com.bstore.common;

/**
 * create by Jakarta
 */
public class Const {
    //当前用户
    public static final String CURRENT_USER = "currentUser";

    //用户登录email和用户名
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    //用户角色
    public interface Role{
        int ROLE_CUSTOMER = 0;  //普通用户
        int ROLE_ADMIN = 1;     //管理员
    }
}
