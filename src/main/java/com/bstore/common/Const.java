package com.bstore.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * create by Jakarta
 */
public class Const {
    //当前用户
    public static final String CURRENT_USER = "currentUser";

    //用户登录email和用户名
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    //用户角色
    public interface Role{
        int ROLE_CUSTOMER = 0;  //普通用户
        int ROLE_ADMIN = 1;     //管理员
    }

    //排序
    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    //购物车选中状态
    public interface Cart{
        int CHECKED = 1; //选中状态
        int UN_CHECKED = 0; //未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

    //商品在线状态维护
    public enum ProductStatusEnum{
        ON_SALE(1,"在售"),
        UN_SQLE(0,"已下架");

        private Integer code;
        private String value;

        ProductStatusEnum(Integer code, String value){
            this.code = code;
            this.value = value;
        }

        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }
    }

    //订单状态维护
    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"已完成"),
        ORDER_CLOSE(60,"已关闭");

        private Integer code;
        private String value;

        OrderStatusEnum(Integer code, String value){
            this.code = code;
            this.value = value;
        }

        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw  new RuntimeException("没有找到对应的枚举");
        }
    }

    //支付类型维护
    public enum PaymentTypeEnum{
        ONLINE_PAY(1,"在线支付");

        private Integer code;
        private String value;

        PaymentTypeEnum(Integer code, String value) {
            this.code = code;
            this.value = value;
        }

        public Integer getCode() {
            return code;
        }

        public String getValue() {
            return value;
        }

        public static PaymentTypeEnum codeOf(int code){
            //此处values()表示枚举：ONLINE_PAY(1,"在线支付");
            for(PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw  new RuntimeException("没有找到对应的枚举");
        }
    }
}
