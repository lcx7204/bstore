package com.bstore.controller.portal;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.pojo.User;
import com.bstore.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * create by Jakarta
 */
@Controller(value = "/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * 根据用户ID获取购物车信息
     * @param session
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse list(HttpSession session){
        //先判断用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //登录成功后获取购物车信息
        return iCartService.getListByUserId(user.getId());
    }

    /**
     * 添加商品到购物车
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse addCart(HttpSession session, Integer count, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //用户登录后可以添加商品到购物车
        return iCartService.addCart(user.getId(),count,productId);
    }

    /**
     * 更改购车中商品的数量
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @RequestMapping(value = "update.do")
    @ResponseBody
    public ServerResponse updateCart(HttpSession session, Integer count, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //用户登录后，修改购物车商品数量
        return iCartService.updateCart(user.getId(),count,productId);
    }

    /**
     * 删除购物车中的某个商品
     * @param session
     * @param productIds
     * @return
     */
    @RequestMapping(value = "delete_product.do")
    @ResponseBody
    public ServerResponse deleteProduct(HttpSession session,String productIds){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    /**
     * 选中购物车中某个商品
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "select.do")
    @ResponseBody
    public ServerResponse select(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //用户登陆后
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }

    /**
     * 取消选中购物车中的某个商品
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "un_select.do")
    @ResponseBody
    public ServerResponse un_select(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //用户登陆后
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    /**
     * 选中所有商品
     * @param session
     * @return
     */
    @RequestMapping(value = "select_all.do")
    @ResponseBody
    public ServerResponse selectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //用户登陆后
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }

    /**
     * 取消选中所有商品
     * @param session
     * @return
     */
    @RequestMapping(value = "un_select_all.do")
    @ResponseBody
    public ServerResponse unSelectAll(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //用户登陆后
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    /**
     * 查询购物车中商品数量
     * @param session
     * @return
     */
    @RequestMapping(value = "get_cart_product_count.do")
    @ResponseBody
    public ServerResponse getCartProductCount(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createBySuccess(0);   //未登录返回为空
        }
        //登陆后
        return iCartService.getCartProductCount(user.getId());
    }
}
