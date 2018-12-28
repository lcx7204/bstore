package com.bstore.dao;

import com.bstore.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    //自己写的

    //根据用户ID和收货地址ID删除收货地址信息
    int deleteShippingByUserIdANDShiipingId(@Param(value = "userId") Integer userId, @Param(value = "shippingId") Integer shippingId);

    //更新收货地址信息
    int updateShipping(Shipping shipping);

    //选中查看具体的地址
    Shipping selectShipping(@Param(value = "userId") Integer userId, Integer shippingId);

    //根据用户Id查找收货地址列表
    List<Shipping> selectByUserId(Integer userId);
}