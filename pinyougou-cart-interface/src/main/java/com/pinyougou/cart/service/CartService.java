package com.pinyougou.cart.service;

import com.pinyougou.pojo.group.Cart;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车列表
     * @param cartList  购物车列表
     * @param itemId    商品的ID商品的数量
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    /**
     * 从redis中获得购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 向redis存入购物车
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     * 合并两个购物车
     * @param redisCartList
     * @param cookieCartList
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> redisCartList,List<Cart> cookieCartList);
}
