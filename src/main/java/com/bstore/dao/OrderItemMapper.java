package com.bstore.dao;

import com.bstore.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    //自己写的

    /**
     * 批量插入orderItems
     * @param orderItemList
     */
    void batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);

    /**
     * 根据订单号查询订单明细
     * @param orderNo
     * @return
     */
    List<OrderItem> getByOrderNo(Long orderNo);

    /**
     * 用户根据Id和orderNo查询订单明细
     * @param orderNo
     * @param userId
     * @return
     */
    List<OrderItem> getByOrderNoUserId(@Param(value = "orderNo") Long orderNo, @Param(value = "userId") Integer userId);
}