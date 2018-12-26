package com.bstore.dao;

import com.bstore.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    //开始自己写的

    /**
     * 获取商品列表信息
     * @return
     */
    List<Product> selectProductList();

    /**
     * 根据关键字搜索商品信息
     * @return
     */
    List<Product> selectProductByNameORId(@Param(value = "productName") String productName, @Param(value = "productId") Integer productId);

    List<Product> getListByKeyWordORCategoryId(@Param(value = "productName") String productName, @Param(value = "categoryIdList") List<Integer> categoryIdList);
}