package com.bstore.controller.portal;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.pojo.User;
import com.bstore.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * create by Jakarta
 */
@Controller
@RequestMapping(value = "/order/")
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    /**
     * 创建订单
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //创建订单
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 根据用户名和订单编号取消订单
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //删除订单
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     * 获取订单的商品信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //获取订单商品信息
        return iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     * 分页获取订单列表
     * @param session
     * @param pageSize
     * @param pageNum
     * @return
     */
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse getOrderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //分页获取订单列表
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    /**
     * 获取订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse getOrderDetail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //获取订单详情
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }


    //支付模块

    /**
     * 支付宝支付模块
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping(value = "pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请先登录");
        }
        //去支付
        return iOrderService.pay(user.getId(),orderNo);
    }
}
