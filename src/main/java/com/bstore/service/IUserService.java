package com.bstore.service;

import com.bstore.common.ServerResponse;
import com.bstore.pojo.User;

/**
 * create by Jakarta
 */
public interface IUserService {

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    ServerResponse<User> login(String username, String password);
}
