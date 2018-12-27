package com.bstore.service.impl;

import com.bstore.common.Const;
import com.bstore.common.ResponseCode;
import com.bstore.common.ServerResponse;
import com.bstore.dao.CartMapper;
import com.bstore.dao.ProductMapper;
import com.bstore.pojo.Cart;
import com.bstore.pojo.Product;
import com.bstore.service.ICartService;
import com.bstore.util.BigDecimalUtil;
import com.bstore.vo.CartProductVo;
import com.bstore.vo.CartVo;
import com.google.common.base.Splitter;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * create by Jakarta
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Value("${productImage.url}")
    private String productImageUrl;

    /**
     * 根据用户id获取购物车信息
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<CartVo> getListByUserId(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 添加商品到购物车
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    @Override
    public ServerResponse addCart(Integer userId, Integer count, Integer productId) {
        //判断用户名和产品ID是否为空
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //参数正确，先去查购物车表中有没有该商品信息
        Cart cartInfo = cartMapper.selectProductByUserIdANDProductId(userId,productId);
        if(cartInfo != null){
            //将商品数据插入到购物车表中
            Cart cart = new Cart();
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setChecked(Const.Cart.CHECKED);    //选中
            cart.setQuantity(count);
            cartMapper.insert(cart);
        }else {
            //商品已存在，修改商品数量
            count = cartInfo.getQuantity() + count;
            cartInfo.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cartInfo);
        }
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 内部方法，根据UserId查询购物车数据
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        //根据用户Id先查询其购物车信息
        List<Cart> cartList = cartMapper.selectProductByUserId(userId);
        //封装购物网车集合VO
        List<CartProductVo> cartProductVoList = new ArrayList<>();

        //初始化购物车总价
        BigDecimal bigDecimal = new BigDecimal("0");
        //判断购物网车集合是否为空
        if(CollectionUtils.isNotEmpty(cartList)){
            //不为空遍历
            for (Cart cartItem : cartList){
                //封装购物车信息
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(cartItem.getUserId());
                cartProductVo.setProductId(cartItem.getProductId());

                //去判断该商品是否还存在
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if (product != null){
                    //封装商品信息
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());   //库存
                    //再判断库存信息是否够
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        //库存够
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else {
                        //库存不够
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //更新购物车中商品数量为有效数量
                        Cart cart = new Cart();
                        cart.setQuantity(buyLimitCount);
                        cart.setId(cartItem.getId());
                        cartMapper.updateByPrimaryKeySelective(cart);
                    }
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价
                    //单个商品总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    //勾选该商品
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }

                //再判断该商品是否勾选，如果勾选就增加到购物车总价中
                if (cartItem.getChecked() == Const.Cart.CHECKED){
                    bigDecimal = BigDecimalUtil.add(bigDecimal.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }

                //将单个购物车商品VO添加到集合中
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(bigDecimal);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(getAllCheckedStatus(userId));
        cartVo.setImgHost(productImageUrl);

        return cartVo;
    }

    //判断是否被全选
    private boolean getAllCheckedStatus(Integer userId){
        if (userId == null){
            return false;
        }else {
            return cartMapper.selectCartProductsCheckedStatusByUserId(userId)==0;
        }
    }

    /**
     * 更新购物车商品数量
     * @param userId
     * @param count
     * @param productId
     * @return
     */
    @Override
    public ServerResponse updateCart(Integer userId, Integer count, Integer productId) {
        //判断数量和产品ID是否为空
        if(productId == null || count == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //去查找该商品是否再购物车中
        Cart cart = cartMapper.selectProductByUserIdANDProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return null;
    }

    /**
     * 删除购物车商品信息
     * @param userId
     * @param productIds
     * @return
     */
    @Override
    public ServerResponse deleteProduct(Integer userId, String productIds) {
        if(productIds == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<String> productList = Splitter.on(",").splitToList(productIds);
        cartMapper.deleteCartProductByUserIdANDProductId(userId,productList);
        return this.getListByUserId(userId);
    }

    /**
     * 商品选中和未被选中
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    @Override
    public ServerResponse selectOrUnSelect(Integer userId, Integer productId, Integer checked) {
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.checkOrUnCheckProduct(userId,productId,checked);
        return this.getListByUserId(userId);
    }

    /**
     * 根据用户ID查询购物车中的商品数量
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getCartProductCount(Integer userId) {
        int resultCount = cartMapper.getCartProductCount(userId);
        return null;
    }
}
