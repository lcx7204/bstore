package com.bstore.service.impl;

import com.bstore.common.ServerResponse;
import com.bstore.dao.UserMapper;
import com.bstore.pojo.User;
import com.bstore.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * create by Jakarta
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        //检测用户名是否已经存在
        int resultCount = userMapper.checkUserName(username);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //TODO MD5加密
        User user = userMapper.selectLogin(username,password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //将返回给前台的密码设置为空
        user.setPassword(StringUtils.EMPTY);
        //返回给前台
        return ServerResponse.createBySuccess("登录成功",user);
    }
}
