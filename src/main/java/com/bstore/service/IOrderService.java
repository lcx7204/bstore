package com.bstore.service;

import com.bstore.common.ServerResponse;
import com.bstore.vo.OrderVo;
import com.github.pagehelper.PageInfo;

/**
 * create by Jakarata
 */
public interface IOrderService {

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    ServerResponse createOrder(Integer userId,Integer shippingId);

    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse cancel(Integer userId, Long orderNo);

    /**
     * 获取订单的商品信息
     * @param userId
     * @return
     */
    ServerResponse getOrderCartProduct(Integer userId);

    /**
     * 分页获取订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);

    /**
     * 获取订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);


    //管理员

    /**
     * 管理员分页获取订单信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);

    /**
     * 管理员获取订单详情
     * @param orderNo
     * @return
     */
    ServerResponse<OrderVo> manageDetail(Long orderNo);

    /**
     * 管理员搜索订单信息
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);

    /**
     * 发货
     * @param orderNo
     * @return
     */
    ServerResponse<String> orderSendGoods(Long orderNo);

    /**
     * 支付宝支付
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse pay(Integer userId,Long orderNo);
}
