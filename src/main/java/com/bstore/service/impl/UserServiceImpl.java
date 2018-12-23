package com.bstore.service.impl;

import com.bstore.common.Const;
import com.bstore.common.ServerResponse;
import com.bstore.common.TokenCache;
import com.bstore.dao.UserMapper;
import com.bstore.pojo.User;
import com.bstore.service.IUserService;
import com.bstore.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * create by Jakarta
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        //检测用户名是否已经存在
        int resultCount = userMapper.checkUserName(username);
        if (resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //给密码进行MD5加密
        String MDPassword = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,MDPassword);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //将返回给前台的密码设置为空
        user.setPassword(StringUtils.EMPTY);
        //返回给前台
        return ServerResponse.createBySuccess("登录成功",user);
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
        //先检测用户名是否存在
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //再检查Email是否存在
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        //设置用户默认角色
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //给密码进行MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 检测用户名是否合法
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){
            //type不为空开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUserName(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("Email已经存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 根据用户名查找问题
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username){
        //先检测用户名是否存在
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if (validResponse.isSuccess()){
            //用户不存在，不合法，无法查找问题
            return ServerResponse.createByErrorMessage("该用户不存在");
        }
        //根据用户名查找找回密码问题
        String question  = userMapper.selectQuestionByUserName(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccessMessage(question);
        }
        return ServerResponse.createByErrorMessage("未找到密码重置的问题");
    }

    /**
     * 检测用户回答的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int checkCount = userMapper.checkAnswer(username,question,answer);
        if (checkCount > 0){
            //说明用户回答正确（利用UUID生成token）
            String forgetToken = UUID.randomUUID().toString();
            //加入到缓存中并设置有效期
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("密码重置问题答案错误");
    }

    /**
     * 忘记密码后重置密码
     * @param username
     * @param newPassword
     * @param forgetToken
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        //先判断forgetToken是否为空
        if(StringUtils.isBlank(forgetToken)){
            //为空
            return ServerResponse.createBySuccessMessage("forgetToken为空,请传递forgetToken");
        }
        //检测用户名是否存在(防止横向越权)
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在,不予重置密码
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //用户存在情况，进行密码重置(1)判断token是否为空
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token过期，请重新获取");
        }
        //(2)取token进行比较
        if(StringUtils.equals(forgetToken,token)){
            //token一致
            String password = MD5Util.MD5EncodeUtf8(newPassword);
            int resultCount = userMapper.updatePasswordByUserName(username,password);
            if (resultCount > 0){
                return ServerResponse.createBySuccessMessage("密码重置成功");
            }else {
                return ServerResponse.createByErrorMessage("密码重置失败");
            }
        }
        return ServerResponse.createByErrorMessage("密码重置失败");
    }

    /**
     * 登录后修改密码
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
        //防止用户横向越权，要先根据userid和password检测是否存在
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("原密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码修改成功");
        }
        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    /**
     * 用户登录后修改个人信息
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateUserInformation(User user) {
        //先检测email是否被占用
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("Email已经被占用");
        }
        //更新数据
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUsername(user.getUsername());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updteCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updteCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }


    /**
     * 登录成功后根据is获取用户详细信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<User> getUserInformation(Integer userId) {
        //查找用户是否存在
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("当前用户不存在");
        }
        user.setPassword(StringUtils.EMPTY);   //密码置空
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 检测是否是管理员
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
