package com.bstore.controller.backend;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.pojo.Product;
import com.bstore.pojo.User;
import com.bstore.service.IProductService;
import com.bstore.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * create by Jakarta
 */
@Controller
@RequestMapping(value = "/manage/product/")
public class ProductManagerController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    /**
     * 保存或更新商品信息
     *
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //进行保存或更新
            return iProductService.saveOrUpdateProduct(product);
        } else {
            return ServerResponse.createByErrorMessage("没有操作权限");
        }
    }

    /**
     * 更新商品上下架状态
     *
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //更新商品状态
            return iProductService.SetSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("没有操作权限");
        }
    }

    /**
     * 管理端获取商品详细信息
     *
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //更新商品状态
            return iProductService.getManageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("没有操作权限");
        }
    }

    /**
     * 获取商品信息列表
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //更新商品状态
            return iProductService.getManageProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("没有操作权限");
        }
    }

    /**
     * 搜索商品信息
     *
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName, Integer productId, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //更新商品状态
            return iProductService.productSearch(productName, productId, pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("没有操作权限");
        }
    }

    /**
     * 上传图片
     *
     * @param session
     * @param file
     * @return
     */
    @RequestMapping(value = "upload.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file) {
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //接收图片数据
            return iProductService.uploadImage(file);
        } else {
            return ServerResponse.createByErrorMessage("没有操作权限");
        }
    }

    /**
     * 富文本上传图片
     *
     * @param session
     * @param file
     * @param response
     * @return
     */
    @RequestMapping(value = "richtext_img_upload.do", method = RequestMethod.POST)
    @ResponseBody
    public Map richTextUploadImg(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletResponse response) {
        Map resultMap = new HashMap();
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员");
            return resultMap;
        }
        //判断是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            //接收富文本图片数据
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return iProductService.uploadRichTextImage(file,response);
        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "无权限操作");
            return resultMap;
        }
    }
}