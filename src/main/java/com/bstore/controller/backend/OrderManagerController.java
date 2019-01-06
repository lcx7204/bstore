package com.bstore.controller.backend;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.pojo.User;
import com.bstore.service.IOrderService;
import com.bstore.service.IUserService;
import com.bstore.vo.OrderVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * create by Jakarta
 */
@Controller
@RequestMapping(value = "/manage/order/")
public class OrderManagerController {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private IUserService iUserService;

    /**
     * 管理员获取订单列表
     * @param session
     * @param pageNun
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNun, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取所有订单列表
            return iOrderService.manageList(pageNun,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("暂无权限操作");
        }
    }

    /**
     * 管理员获取订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do")
    @ResponseBody
    public ServerResponse<OrderVo> orderDetail(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取订单详情
            return iOrderService.manageDetail(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("暂无权限操作");
        }
    }

    /**
     * 管理员搜索
     * @param session
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do")
    @ResponseBody
    public ServerResponse<PageInfo> search(HttpSession session, Long orderNo, @RequestParam(value = "pageNum") int pageNum, @RequestParam(value = "pageSize") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取订单详情
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("暂无权限操作");
        }
    }

    /**
     * 发货
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGood(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //获取订单详情
            return iOrderService.orderSendGoods(orderNo);
        }else{
            return ServerResponse.createByErrorMessage("暂无权限操作");
        }
    }
}
