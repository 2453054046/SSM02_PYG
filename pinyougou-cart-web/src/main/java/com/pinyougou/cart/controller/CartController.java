package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.group.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    HttpServletResponse response;
    @Autowired
    HttpServletRequest request;

    @Reference(timeout = 10000)
    CartService cartService;

    /**
     * 存入购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9006")//跨域请求注解，默认支持cookie
    public Result addGoodsToCartList(Long itemId,Integer num){//http://localhost:9005/cart/addGoodsToCartList?itemId=1369305&num=2
        /**
         * 指定跨域请求  头信息：Access-Control-Allow-Origin  跨域的地址：http://localhost:9006  用*代表所有地址，但是不能使用cookie
         */
       /* response.setHeader("Access-Control-Allow-Origin", "http://localhost:9006");
        *//**
         * 指定跨域请求可以携带cookie
         *//*
        response.setHeader("Access-Control-Allow-Credentials", "true");*/
        //从Security中获得登陆的用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人："+username);
        try {
            //提取购物车
            List<Cart> cartList =  findCartList();
            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            if(username.equals("anonymousUser")){//未登陆
                System.out.println("向cookie中存储购物车");
                //将新的购物车存入cookie
                String jsonString = JSON.toJSONString(cartList);
                util.CookieUtil.setCookie(request,response,"cartList",jsonString,3600*24*7,"UTF-8");
            }else {//登陆
                cartService.saveCartListToRedis(username,cartList);
            }


            return new Result(true,"存入购物车成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"存入购物车失败");
        }
    }

    /**
     * 从cookie中图区购物车
     * @return
     */
    @RequestMapping("/findCartList")
    private List<Cart> findCartList() {
        //从Security中获得登陆的用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //获得cookie中的购物车列表
        String cartList = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if(cartList==null||cartList.equals("") ||cartList.equals("null")){
            cartList="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList,Cart.class);//将cookie中的数据转换购物车列表对象
        if(username.equals("anonymousUser")){//当用户未登陆时，username的值为anonymousUser
            //从cookie中提取购物车
            System.out.println("从cookie中提取购物车");
            return cartList_cookie;
        }else {//登陆
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            if(cartList_cookie.size()>0){//判断cookie中是否存在购物车，如果存在合并购物车到用户的redis购物车中
                cartListFromRedis = cartService.mergeCartList(cartList_cookie, cartListFromRedis);
                cartService.saveCartListToRedis(username,cartListFromRedis);                //更新redis购物车
                util.CookieUtil.deleteCookie(request,response,"cartList");       //清除cookie
            }
            return cartListFromRedis;
        }
    }

}
