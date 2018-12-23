package com.bstore.dao;

import com.bstore.pojo.Category;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);

    //自己写的代码

    //获取平级商品品类节点ID
    List<Category> getCategoty(Integer parentId);
}