package com.bstore.service.impl;

import com.bstore.common.ServerResponse;
import com.bstore.dao.CategoryMapper;
import com.bstore.pojo.Category;
import com.bstore.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * create by Jakarta
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 增加商品品类
     * @param categoryName
     * @param parentId
     * @return
     */
    @Override
    public ServerResponse addCateGory(String categoryName, Integer parentId) {
        //判断参数信息是否错误
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("参数信息传递错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);   //表示这个分类是可用的
        int resultCount = categoryMapper.insert(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("品类信息添加成功!");
        }
        return ServerResponse.createByErrorMessage("品类信息添加失败");
    }

    /**
     * 修改商品品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @Override
    public ServerResponse updateCategory(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("参数信息传递错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("商品品类信息更新成功");
        }
        return ServerResponse.createByErrorMessage("商品品类信息更新失败");
    }

    /**
     * 获取平级品类节点信息
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Category>> getCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.getCategoty(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){  //判断是否是空集合
            //打印空集合日志
            logger.info("未找到当前分类的子分类");
        }
        return null;
    }

    /**
     * 获取当前分类id及递归其子节点的categoryId存到一个List中一块返回给前台
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> getDeepCategory(Integer categoryId){
        //利用Set存储数据，无序且不重复，查询较快，要重写hashcode和equals方法
        Set<Category> categorySet = new HashSet<>();
        //递归算出子节点
        findChildCategory(categorySet,categoryId);

        List<Integer> categoryList = new ArrayList<Integer>();
        if(categoryId != null){
            //遍历集合
            for (Category categoryItem:categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归遍历算出子节点
     * @param categorySet
     * @param categoryId
     * @return
     */
    public Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        //先查找categoryId是否存在(找自己)
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null){
            categorySet.add(category);
        }
        //再去找子节点,递归算法要有退出条件，否则会死循环（找孩子）
        List<Category> categoryList = categoryMapper.getCategoty(categoryId);
        for(Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }
}
