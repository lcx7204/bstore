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
}