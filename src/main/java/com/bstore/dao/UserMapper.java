package com.bstore.dao;

import com.bstore.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //自己写的方法开始

    //检测用户名是否存在
    int checkUserName(String username);

    //检测邮箱是否存在
    int checkEmail(String email);

    //用户登录(mybatis传递多个参数时要使用注解申声明,注解中的名字要和SQL中的变量名对应)
    User selectLogin(@Param("username") String username, @Param("password") String password);

    //根据用户名查找找回密码问题
    String selectQuestionByUserName(String username);

    //检测用户提交的答案是否合法
    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    //根据用户名和密码更新密码
    int updatePasswordByUserName(@Param("username") String username,@Param("password") String password);

    //根据userid和password检测是否存在
    int checkPassword(@Param("password") String password,@Param("userId") Integer userId);

    //根据userId检测Email是否被占用
    int checkEmailByUserId(@Param("email") String email,@Param("userId") Integer userId);
}