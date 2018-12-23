package com.bstore.service;

import com.bstore.common.ServerResponse;
import com.bstore.pojo.Category;

import java.util.List;

/**
 * create by Jakarta
 */
public interface ICategoryService {
    /**
     * 增加商品品类
     * @param categoryName
     * @param parentId
     * @return
     */
    ServerResponse addCateGory(String categoryName,Integer parentId);

    /**
     * 更新品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    ServerResponse updateCategory(Integer categoryId, String categoryName);

    /**
     * 获取平级品类节点信息
     * @param categoryId
     * @return
     */
    ServerResponse<List<Category>> getCategory(Integer categoryId);

    /**
     * 获取当前分类id及递归其子节点的categoryId存到一个List中一块返回给前台
     * @param categoryId
     * @return
     */
    ServerResponse<List<Integer>> getDeepCategory(Integer categoryId);
}
