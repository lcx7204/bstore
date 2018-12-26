package com.bstore.service.impl;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.dao.CategoryMapper;
import com.bstore.dao.ProductMapper;
import com.bstore.pojo.Category;
import com.bstore.pojo.Product;
import com.bstore.service.ICategoryService;
import com.bstore.service.IFileService;
import com.bstore.service.IProductService;
import com.bstore.util.DateTimeUtil;
import com.bstore.vo.ProductDetailVo;
import com.bstore.vo.ProductListVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private IFileService iFileService;

    @Autowired
    private ICategoryService iCategoryService;

    @Value("${productImage.savePath}")
    private String productImageSavePath;
    
    @Value("${productImage.url}")
    private String productImageUrl;
    /**
     * 保存或更改商品信息
     * @param product
     * @return
     */
    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        //判断product是否为空
        if (product == null){
            return ServerResponse.createByErrorMessage("商品参数信息传递错误");
        }
        //检测有没有上传图片地址,用逗号分隔
        if(StringUtils.isNotBlank(product.getSubImages())){
            String[] subImageArr = product.getSubImages().split(",");
            if(subImageArr.length > 0){
                //默认将第一张图片的地址赋值给主图
                product.setMainImage(subImageArr[0]);
            }
        }
        //如果没有id,就是更新
        if(product.getId() != null){
            int resultCount = productMapper.updateByPrimaryKey(product);
            if (resultCount > 0){
                return ServerResponse.createBySuccessMessage("更新商品信息成功");
            }
        }else {
            //保存操作
            int resultCount = productMapper.insert(product);
            if(resultCount > 0){
                return ServerResponse.createBySuccessMessage("新增商品信息成功");
            }
            return ServerResponse.createByErrorMessage("保存商品信息失败");
        }
        return ServerResponse.createByErrorMessage("新增或修改商品信息失败");
    }

    /**
     * 更新商品上下架状态
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse SetSaleStatus(Integer productId, Integer status) {
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("更新商品上下架状态成功");
        }
        return ServerResponse.createByErrorMessage("更新商品上下架状态失败");
    }

    /**
     * 管理端获取商品详细信息
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> getManageProductDetail(Integer productId) {
        if (productId == null){
            return ServerResponse.createByErrorMessage("获取商品详情信息参数传递错误");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product !=null){
            //将Product的值传递给ProductDetailVo
            ProductDetailVo productDetailVo = assembleProductDetailVo(product);
            return ServerResponse.createBySuccess(productDetailVo);
        }
        return ServerResponse.createByErrorMessage("商品不存在");
    }

    //将Product的值传递给ProductDetailVo
    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        //自定义日期字符串格式转换
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        //图片地址前缀
        productDetailVo.setImageHost(productImageUrl);
        //商品所属的父类Id
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
			productDetailVo.setParentCategoryId(0);	//默认设置为根节点
		}else {
			productDetailVo.setParentCategoryId(category.getParentId());
		}
        
        return productDetailVo;
    }

    /**
     * 分页获取商品信息列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getManageProductList(int pageNum, int pageSize) {
        //start-page
        PageHelper.startPage(pageNum, pageSize);
        //填充业务逻辑
        //查找Product列表
        List<Product> productList = productMapper.selectProductList();
        //将pojo传递给VO
        List<ProductListVo> productListVo = new ArrayList<>();
        ProductListVo productlVo = null;
        for(Product productItem : productList){
            productlVo = new ProductListVo();
            productlVo.setId(productItem.getId());
            productlVo.setName(productItem.getName());
            productlVo.setCategoryId(productItem.getCategoryId());
            productlVo.setMainImage(productItem.getMainImage());
            productlVo.setStatus(productItem.getStatus());
            productlVo.setPrice(productItem.getPrice());
            productlVo.setImageHost(productImageUrl);
            productListVo.add(productlVo);
        }
        //pageHelper收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVo);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 搜索商品列表
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> productSearch(String productName, int productId, int pageNum, int pageSize) {
        if(StringUtils.isNotBlank(productName)){
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList = productMapper.selectProductByNameORId(productName,productId);
        //将pojo传递给VO
        List<ProductListVo> productListVo = new ArrayList<>();
        ProductListVo productlVo = null;
        for(Product productItem : productList){
            productlVo = new ProductListVo();
            productlVo.setId(productItem.getId());
            productlVo.setName(productItem.getName());
            productlVo.setCategoryId(productItem.getCategoryId());
            productlVo.setMainImage(productItem.getMainImage());
            productlVo.setStatus(productItem.getStatus());
            productlVo.setPrice(productItem.getPrice());
            productlVo.setImageHost(productImageUrl);
            productListVo.add(productlVo);
        }
        //pageHelper收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVo);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @Override
    public ServerResponse uploadImage(MultipartFile file) {
        String path = productImageSavePath;
        String targetName = iFileService.uploadFile(file,path);
        if (StringUtils.isBlank(targetName)){
            return ServerResponse.createByErrorMessage("图片上传失败");
        }
        Map fileMap = new HashMap();
        fileMap.put("uri",targetName);
        fileMap.put("url",productImageUrl+targetName);
        return ServerResponse.createBySuccess(fileMap);
    }

    /**
     * 富文本上传图片
     * @param file
     * @return
     */
    @Override
    public Map uploadRichTextImage(MultipartFile file,HttpServletResponse response) {
        Map resultMap = new HashMap();
        String path = productImageSavePath;
        String targetName = iFileService.uploadFile(file,path);
        if(StringUtils.isBlank(targetName)){
            resultMap.put("success",false);
            resultMap.put("msg","图片上传失败");
            return resultMap;
        }
        resultMap.put("success",true);
        resultMap.put("msg","图片上传成功");
        resultMap.put("uri",targetName);
        resultMap.put("url",productImageUrl+targetName);
        response.addHeader("Access-Control-Allow-Headers","X-File-Name");
        return resultMap;
    }

    //门户网站service

    /**
     * 门户网站根据关键字搜索，排序获取列表
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderby
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getListByKeyWordORCategoryId(String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderby) {
        if(StringUtils.isBlank(keyword) && categoryId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //一个用于存放categoryId的集合
        List<Integer> categoryIdList = new ArrayList<Integer>();
        if(categoryId != null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)){
                //如果没有该分类并且关键字为空,就返回空集合
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = new ArrayList<>();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
        }
        categoryIdList = iCategoryService.getDeepCategory(categoryId).getData();  //深度遍历节点及其子节点
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuffer().append("%").append(keyword).append("%").toString();
        }
        //分页开始
        PageHelper.startPage(pageNum,pageSize);
        //逻辑处理
        if(StringUtils.isNotBlank(orderby)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(keyword)){
                String[] orderByArr = orderby.split("_");
                PageHelper.orderBy(orderByArr[0]+" "+orderByArr[1]);   //结果为SQL语句，会动态拼接 order by price desc/asc
            }
        }

        List<Product> productList = productMapper.getListByKeyWordORCategoryId(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
        //转换
        List<ProductListVo> productListVoList = new ArrayList<>();
        ProductListVo productListVo = null;
        for(Product productItem : productList){
            productListVo = new ProductListVo();
            productListVo.setId(productItem.getId());
            productListVo.setName(productItem.getName());
            productListVo.setCategoryId(productItem.getCategoryId());
            productListVo.setMainImage(productItem.getMainImage());
            productListVo.setStatus(productItem.getStatus());
            productListVo.setPrice(productItem.getPrice());
            productListVo.setImageHost(productImageUrl);
            productListVo.setImageHost(productImageUrl);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
