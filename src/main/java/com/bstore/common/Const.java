package com.bstore.common;

import com.google.common.collect.Sets;

import java.util.Set;

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

    //排序
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    //购物车选中状态
    public interface Cart{
        int CHECKED = 1; //选中状态
        int UN_CHECKED = 0; //未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }
}
