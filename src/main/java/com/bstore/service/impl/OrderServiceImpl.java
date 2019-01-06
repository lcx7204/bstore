package com.bstore.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.bstore.common.Const;
import com.bstore.common.ServerResponse;
import com.bstore.dao.*;
import com.bstore.pojo.*;
import com.bstore.service.IOrderService;
import com.bstore.util.BigDecimalUtil;
import com.bstore.util.DateTimeUtil;
import com.bstore.vo.OrderItemVo;
import com.bstore.vo.OrderProductVo;
import com.bstore.vo.OrderVo;
import com.bstore.vo.ShippingVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * create by Jakarta
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService{

    private static AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Value("${productImage.url}")
    private String productImageUrl;

    @Value("${alipay.callback.url}")
    private String alipayCallBackUrl;

    @Value("${alipay.pic.url}")
    private String alipayPicUrl;

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //先判断购物车中货物商品信息
        List<Cart> cartList = cartMapper.selectProductByUserId(userId);

        //#start 从订单子表获取商品总价
        ServerResponse serverResponse = this.getOrderItems(userId,cartList);
        if(!serverResponse.isSuccess()){
            //失败
            return serverResponse;
        }
        //成功，计算总价
        List<OrderItem> orderItemList = (List<OrderItem>)serverResponse.getData();
        BigDecimal totalPrice = this.getTotalPrice(orderItemList);
        //#end 从订单子表获取商品总价

        //#start 生成订单
        Order order = assembleOrder(userId,shippingId,totalPrice);
        if(order == null){
            return ServerResponse.createByErrorMessage("生成订单失败");
        }
        //#end 生成订单

        //判断购物车是否为空
        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //给orderItem赋值订单号
        for (OrderItem orderItem : orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis批量插入
        orderItemMapper.batchInsert(orderItemList);

        //减少产品库存
        this.reduceProductStock(orderItemList);
        //清空购物车
        this.cleanCart(cartList);

        //组装数据返回前端
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    private void cleanCart(List<Cart> cartList){
        for(Cart cart : cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }


    /**
     * 根据购物车里的商品生成订单子表
     * @param userId
     * @param cartList
     * @return
     */
    private ServerResponse getOrderItems(Integer userId, List<Cart> cartList){
        List<OrderItem> orderItemList = new ArrayList<>();
        if(CollectionUtils.isEmpty(cartList)){
            //如果购物车集合为空
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //遍历购物车集合(校验购物车中的商品数据，状态，库存)
        for(Cart cartItem : cartList){
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //根据product中的状态判断购物车中的状态
            if(Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()){
                //下架状态
                return ServerResponse.createByErrorMessage("商品"+product.getName()+"已下架");
            }
            //判断库存
            if(cartItem.getQuantity() > product.getStock()){
                return ServerResponse.createByErrorMessage("商品"+product.getName()+"库存不足");
            }
//               orderItem表
//              `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '订单子表id',
//              `user_id` int(11) DEFAULT NULL,
//              `order_no` bigint(20) DEFAULT NULL,
//              `product_id` int(11) DEFAULT NULL COMMENT '商品id',
//              `product_name` varchar(100) DEFAULT NULL COMMENT '商品名称',
//              `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片地址',
//              `current_unit_price` decimal(20,2) DEFAULT NULL COMMENT '生成订单时的商品单价，单位是元,保留两位小数',
//              `quantity` int(10) DEFAULT NULL COMMENT '商品数量',
//              `total_price` decimal(20,2) DEFAULT NULL COMMENT '商品总价,单位是元,保留两位小数',
//              `create_time` datetime DEFAULT NULL,
//              `update_time` datetime DEFAULT NULL,
            //排除异常，生成订单子表
            orderItem.setUserId(userId);
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartItem.getQuantity()));
//            把订单子表放到集合中
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 根据订单子表查询总价
     * @param orderItemList
     * @return
     */
    private BigDecimal getTotalPrice(List<OrderItem> orderItemList){
        //初始化总价
        BigDecimal totalPrice = new BigDecimal("0");
        for(OrderItem orderItem : orderItemList){
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
        }
        return totalPrice;
    }

    /**
     * 生成订单
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment){
        Order order = new Order();
        //生成订单号
        long orderNo = generateOrderNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payment);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        int rowCount = orderMapper.insert(order);
        if(rowCount > 0){
            //插入成功
            return order;
        }
        return null;
    }

    private long generateOrderNo(){
        long currentTime =System.currentTimeMillis();
        return currentTime+new Random().nextInt(100);
    }

    /**
     * 根据订单及订单明细生成订单VO
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList){
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getStatus()).getValue());    //在线支付
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());          //支付状态
        orderVo.setShippingId(order.getShippingId());

        //查询收货地址详细信息
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping != null){
            orderVo.setReceiveName(shipping.getReceiverName());
            //把收货地址对象放到OrderVo中
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setImageHost(productImageUrl);

        //遍历订单明细
        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for(OrderItem orderItem : orderItemList){
            //组装OrderItemVo
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }

    /**
     * 组装ShippingVo
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping){
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private OrderItemVo assembleOrderItemVo(OrderItem orderItem){
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }

    private void reduceProductStock(List<OrderItem> orderItemList){
        for (OrderItem orderItem : orderItemList){
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }


    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectOrderByUserIdANDOrderNo(userId,orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("该用户订单不存在");
        }
        if(order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) { // 如果订单状态不等于未支付
            return ServerResponse.createByErrorMessage("该订单已付款，无法取消");
        }

        Order updateOrder = new Order();
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        updateOrder.setId(order.getId());

        int resultRow = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (resultRow > 0){
            return ServerResponse.createBySuccessMessage("订单取消成功");
        }
        return ServerResponse.createByErrorMessage("订单取消失败");
    }

    /**
     * 获取订单的商品信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();

        List<Cart> cartList = cartMapper.selectProductByUserId(userId);
        ServerResponse serverResponse = this.getOrderItems(userId,cartList);    //子订单
        if(!serverResponse.isSuccess()){
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = new ArrayList<>();

        BigDecimal totalPrice = new BigDecimal("0");
        for(OrderItem orderItem: orderItemList){
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(totalPrice);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(productImageUrl);
        return ServerResponse.createBySuccess(orderProductVo);
    }


    /**
     * 分页获取订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        //根据用户Id查询订单列表按时间降序排列
        List<Order> orderList = orderMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVo> orderVoList = assembleOrderVoList(userId,orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    private List<OrderVo> assembleOrderVoList(Integer userId, List<Order> orderList){
        List<OrderVo> orderVoList = new ArrayList<>();
        for(Order order : orderList){
            List<OrderItem> orderItemList = new ArrayList<>();
            if(userId == null){
                //说明是管理员，查询所有列表
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            }else {
                //普通用户
                orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(),userId);
            }
            OrderVo orderVo = assembleOrderVo(order,orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 获取订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectOrderByUserIdANDOrderNo(userId,orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("此用户不存在该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        OrderVo orderVo = assembleOrderVo(order,orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }


    /**
     * 管理员分页获取订单列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        //查询所有订单
        List<Order> orderList = orderMapper.selectAllOrder();
        //封装返回VO
        List<OrderVo> orderVoList = this.assembleOrderVoList(null,orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 管理员获取订单详情
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order == null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    /**
     * 管理员搜索订单信息
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order != null){
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order,orderItemList);
            PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));
            pageInfo.setList(Lists.newArrayList(orderNo));
            return ServerResponse.createBySuccess(pageInfo);
        }
        return ServerResponse.createByErrorMessage("订单号不存在");
    }

    /**
     * 发货
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<String> orderSendGoods(Long orderNo) {
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order != null){
            if(order.getStatus() == Const.OrderStatusEnum.PAID.getCode()){
                //已付款
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ServerResponse.createBySuccessMessage("发货成功");
            }
        }
        return ServerResponse.createByErrorMessage("此订单信息不存在");
    }


    /**
     * 支付宝支付
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse pay(Integer userId, Long orderNo) {
        Map<String,String> resultMap = Maps.newHashMap();
        //查看订单是否存在
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order == null){
            return ServerResponse.createByErrorMessage("此用户该订单号不存在");
        }
        resultMap.put("orderNo",order.getOrderNo().toString());

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("lcx书店商城扫码支付,订单号为:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totoalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totoalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        //支付宝超时时间，120min
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        //遍历商品明细
        for (OrderItem orderItem : orderItemList){
            //组装商品明细到支付宝提供的集合中
            GoodsDetail goodsDetail = GoodsDetail.newInstance(orderItem.getProductId().toString(),
                    orderItem.getProductName(),BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),
                            new Double(100).doubleValue()).longValue(),orderItem.getQuantity());
            goodsDetailList.add(goodsDetail);
        }

        //创建扫码支付请求Builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder().setSubject(subject).setTotalAmount(totoalAmount)
                .setOutTradeNo(outTradeNo).setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body).setOperatorId(operatorId)
                .setStoreId(storeId).setExtendParams(extendParams).setNotifyUrl(alipayCallBackUrl).setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()){
            case SUCCESS:
                logger.info("支付宝预下单成功");
                //下单成功，将二维码上传至服务器
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                String path = alipayPicUrl;
                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                //细节细节
                String qrpath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(),256,qrpath);
                File targetFile = new File(path,qrFileName);
                logger.info("qrPath:" + qrpath);


            case FAILED:
                logger.error("支付宝预下单失败");
                return ServerResponse.createByErrorMessage("支付宝预下单失败");
            case UNKNOWN:
                logger.error("系统异常，支付宝预下单状态未知");
                return ServerResponse.createByErrorMessage("系统异常，支付宝预下单状态未知");
            default:
                logger.error("不支持的交易状态，返回异常");
                return ServerResponse.createByErrorMessage("不支持的交易状态，返回异常");
        }
    }

    public void dumpResponse(AlipayResponse response){
        if (response != null){
            logger.info(String.format("code:%s, msg:%s",response.getCode(),response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())){
                logger.info(String.format("subCode:%s,subMsg:%s",response.getSubCode(),response.getSubCode(),response.getSubMsg()));
            }
            logger.info("body:"+response.getBody());
        }
    }
}
