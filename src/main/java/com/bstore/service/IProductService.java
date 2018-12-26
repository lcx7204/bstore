package com.bstore.service;

import com.bstore.common.ServerResponse;
import com.bstore.pojo.Product;
import com.bstore.vo.ProductDetailVo;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * create by Jakarta
 */
public interface IProductService {
    /**
     * 保存或修改商品信息
     * @param product
     * @return
     */
    ServerResponse saveOrUpdateProduct(Product product);

    /**
     * 更新商品上下架状态
     * @param productId
     * @param status
     * @return
     */
    ServerResponse SetSaleStatus(Integer productId, Integer status);

    /**
     * 管理端获取商品详细信息
     * @param productId
     * @return
     */
    ServerResponse<ProductDetailVo> getManageProductDetail(Integer productId);

    /**
     * 分页获取商品信息列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> getManageProductList(int pageNum, int pageSize);

    /**
     * 搜索商品列表
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse<PageInfo> productSearch(String productName,int productId, int pageNum, int pageSize);

    /**
     * 上传图片
     * @param file
     * @return
     */
    ServerResponse uploadImage(MultipartFile file);

    /**
     * 富文本上传图片
     * @param file
     * @return
     */
    Map uploadRichTextImage(MultipartFile file, HttpServletResponse response);

    //门户网站开始

    /**
     * 门户网站根据关键字搜索，排序获取列表
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderby
     * @return
     */
    ServerResponse<PageInfo> getListByKeyWordORCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderby);
}
