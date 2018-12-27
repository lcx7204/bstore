package com.bstore.dao;

import com.bstore.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    //自己写的方法

    /**
     * 查询购物车中是否有该商品信息
     * @param userId
     * @param productId
     * @return
     */
    Cart selectProductByUserIdANDProductId(@Param(value = "userId") Integer userId, @Param(value = "productId") Integer productId);

    /**
     * 根据用户Id查询购物车集合
     * @param userId
     * @return
     */
    List<Cart> selectProductByUserId(Integer userId);

    /**
     * 查询是否有未勾选的商品
     * @param userId
     * @return
     */
    int selectCartProductsCheckedStatusByUserId(Integer userId);

    /**
     * 删除购物车商品信息
     * @param userId
     * @param productList
     * @return
     */
    int deleteCartProductByUserIdANDProductId(@Param(value = "userId") Integer userId, @Param(value = "productList") List<String> productList);

    /**
     * 选中和未被选中商品(单个)
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    int checkOrUnCheckProduct(@Param(value = "userId") Integer userId,@Param(value = "productId") Integer productId,@Param(value = "checked") Integer checked);

    /**
     * 根据用户ID查询购物车中的商品数量
     * @param userId
     * @return
     */
    int getCartProductCount(Integer userId);
}