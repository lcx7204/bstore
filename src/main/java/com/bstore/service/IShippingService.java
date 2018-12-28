package com.bstore.service;

import com.bstore.common.ServerResponse;
import com.bstore.pojo.Shipping;
import com.github.pagehelper.PageInfo;

/**
 * create by Jakarta
 */
public interface IShippingService {
    /**
     * 添加收货地址
     * @param userId
     * @param shipping
     * @return
     */
    ServerResponse add(Integer userId, Shipping shipping);

    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    ServerResponse del(Integer userId, Integer shippingId);

    /**
     * 修改收货地址
     * @param userId
     * @param shipping
     * @return
     */
    ServerResponse updateShipping(Integer userId, Shipping shipping);

    /**
     * 选中查看具体的地址
     * @param userId
     * @param shippingId
     * @return
     */
    ServerResponse<Shipping> selectShipping(Integer userId, Integer shippingId);

    /**
     * 收货地址列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> list(Integer userId,int pageNum,int pageSize);
}
