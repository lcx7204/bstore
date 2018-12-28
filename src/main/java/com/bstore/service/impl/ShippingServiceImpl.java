package com.bstore.service.impl;

import com.bstore.common.ServerResponse;
import com.bstore.dao.ShippingMapper;
import com.bstore.pojo.Shipping;
import com.bstore.service.IShippingService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * create by Jakarta
 */
@Service(value = "iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 添加收货地址
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int resultCount = shippingMapper.insert(shipping);
        if (resultCount > 0){
            Map result = new HashMap();
            result.put("shippingId",shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新增地址失败");
    }

    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        int resultCount = shippingMapper.deleteShippingByUserIdANDShiipingId(userId,shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("删除收货地址成功");
        }
        return ServerResponse.createByErrorMessage("删除收货地址失败");
    }

    /**
     * 修改收货地址
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse updateShipping(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        int resultCount = shippingMapper.updateShipping(shipping);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新收货地址信息成功");
        }
        return ServerResponse.createByErrorMessage("更新收货地址信息失败");
    }

    /**
     * 选中查看具体的地址
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse selectShipping(Integer userId, Integer shippingId) {
        Shipping shipping = new Shipping();
        shipping = shippingMapper.selectShipping(userId,shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法找到该地址信息");
        }
        return ServerResponse.createBySuccess("找到该地址信息",shipping);
    }

    /**
     * 获取收货地址信息列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
