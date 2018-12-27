package com.bstore.service;

import com.bstore.common.ServerResponse;

/**
 * create by Jakarta
 */
public interface ICartService {

    /**
     * 根据用户id获取购物车信息
     * @param userId
     * @return
     */
    ServerResponse getListByUserId(Integer userId);

    /**
     * 添加商品到购物车
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    ServerResponse addCart(Integer userId, Integer count, Integer productId);

    /**
     * 修改购物车商品数量
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    ServerResponse updateCart(Integer userId, Integer count, Integer productId);

    /**
     * 删除购物车商品
     * @param userId
     * @param productIds
     * @return
     */
    ServerResponse deleteProduct(Integer userId, String productIds);

    /**
     * 商品选中和未被选中
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    ServerResponse selectOrUnSelect(Integer userId,Integer productId,Integer checked);

    /**
     * 根据用户ID查询购物车中的商品数量
     * @param userId
     * @return
     */
    ServerResponse getCartProductCount(Integer userId);

}
