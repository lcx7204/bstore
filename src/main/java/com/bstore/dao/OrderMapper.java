package com.bstore.dao;

import com.bstore.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    //自己写的

    /**
     * 根据用户Id和orderNo查询订单信息
     * @param userId
     * @param orderNo
     * @return
     */
    Order selectOrderByUserIdANDOrderNo(@Param(value = "userId") Integer userId, @Param(value = "orderNo") Long orderNo);

    /**
     * 根据用户Id查找订单集合
     * @param userId
     * @return
     */
    List<Order> selectByUserId(Integer userId);

    /**
     * 管理员获取订单集合
     * @return
     */
    List<Order> selectAllOrder();

    /**
     * 管理员根据订单号查询订单详情
     * @param orderNo
     * @return
     */
    Order selectOrderByOrderNo(Long orderNo);

}