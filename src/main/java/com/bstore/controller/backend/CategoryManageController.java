package com.bstore.controller.backend;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.pojo.User;
import com.bstore.service.ICategoryService;
import com.bstore.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * create by Jakarta
 */
@Controller
@RequestMapping(value = "/manage/category/")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 后台添加商品品类信息
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        //先判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请进行管理员登录");
        }
        //校验是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员，增加品类信息
            return iCategoryService.addCateGory(categoryName, parentId);
        }else{
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    /**
     * 更改商品品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId, String categoryName){
        //先判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请进行管理员登录");
        }
        //校验是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员，更新品类名称
            return iCategoryService.updateCategory(categoryId,categoryName);
        }else{
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    /**
     * 获取平级品类节点
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_gategory.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){  //默认id为0,根节点
        //先判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请进行管理员登录");
        }
        //校验是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员，获取品类评级节点
            return iCategoryService.getCategory(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    /**
     * 获取当前分类id及递归子节点categoryId
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_category.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getDeepCategory(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId){
        //先判断用户是否登录
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"请进行管理员登录");
        }
        //校验是否是管理员登录
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是管理员，查询当前子节点的id并递归子节点的categoryId
            return iCategoryService.getDeepCategory(categoryId);
        }else{
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }
}
