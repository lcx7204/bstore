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

    /**
     * 用户注册
     * @param user
     * @return
     */
    ServerResponse<String> register(User user);

    /**
     * 检测用户名是否合法
     * @param str
     * @param type
     * @return
     */
    ServerResponse<String> checkValid(String str,String type);

    /**
     * 根据用户名查找问题
     * @param username
     * @return
     */
    ServerResponse<String> selectQuestion(String username);

    /**
     * 检测用户回答的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    ServerResponse<String> checkAnswer(String username,String question,String answer);

    /**
     * 忘记密码后重置密码
     * @param username
     * @param newPassword
     * @param forgetToken
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username,String newPassword,String forgetToken);

    /**
     * 登陆后修改密码
     * @param oldPassword
     * @param newPassword
     * @return
     */
    ServerResponse<String> resetPassword(String oldPassword,String newPassword,User user);

    /**
     * 用户登录后修改个人信息
     * @param user
     * @return
     */
    ServerResponse<User> updateUserInformation(User user);

    /**
     * 登录后根据id获取个人详细信息
     * @param userId
     * @return
     */
    ServerResponse<User> getUserInformation(Integer userId);

}
