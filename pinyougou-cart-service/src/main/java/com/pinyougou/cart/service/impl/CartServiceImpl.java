package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.group.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //商品详情表
    @Autowired
    TbItemMapper tbItemMapper;

    //redis
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 添加商品到购物车列表
     * @param cartList  购物车列表
     * @param itemId    商品的ID
     * @param num       商品的数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据skuID查询商品明细SKU的对象
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        if(tbItem==null){
            throw  new RuntimeException("商品不存在");//报出异常
        }
        if(!tbItem.getStatus().equals("1")){
            throw new RuntimeException("商品不合法");
        }
        //2.根据SKU对象得到商家ID
        String sellerId = tbItem.getSellerId();
        //3.根据商家ID在购物车列表中查询购物车对象
        Cart cart = searchCartBySellerId(cartList,sellerId);
        if(cart==null){//4.如果购物车列表中不存在该商家的购物车
            //4.1 创建一个新的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(tbItem.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            TbOrderItem orderItem = createOrderItem(tbItem,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2将新的购物车对象添加到购物车列表中
            cartList.add(cart);
        }else {//5.如果购物车列表中存在该商家的购物车
            // 判断该商品是否在该购物车的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem==null){
                //5.1  如果不存在  ，创建新的购物车明细对象，并添加到该购物车的明细列表中
                orderItem = createOrderItem(tbItem,num);
                cart.getOrderItemList().add(orderItem);
            }else {
                //5.2 如果存在，在原有的数量上添加数量 ,并且更新金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
            }
        }
        return cartList;
    }

    /**
     * 从redis中获得购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 向redis存入购物车
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    /**
     * 合并两个购物车
     * @param redisCartList
     * @param cookieCartList
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> redisCartList, List<Cart> cookieCartList) {
        //防止重复商品 遍历购物车的商品向另一个购物车内加入
        for(Cart cart:cookieCartList){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                redisCartList = addGoodsToCartList(redisCartList,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return redisCartList;
    }

    /**
     * 判断商品是否存在购物车
     * @param orderItemList     购物车
     * @param itemId            商品id
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem:orderItemList){
            if(orderItem.getItemId().longValue()==itemId.longValue()){//Long类型没有equals方法需要转换比较值
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车明细对象
     * @param item    商品详情
     * @param num       商品的数量
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(  new BigDecimal(item.getPrice().doubleValue()*num) );//商品数量
        return orderItem;
    }

    /**
     * 根据商家ID获得购物车列表中对应商家的购物车，如果购物车列表没有对应的商家购物车返回null
     * @param cartList  购物车列表
     * @param sellerId  商家ID
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for(Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
}
