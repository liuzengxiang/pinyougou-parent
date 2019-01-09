package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;
import vo.CartVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    @RequestMapping("/findCartList")
    public List<CartVo> findCartList(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(name+"=========================================");
        //从cookie中提取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || "".equals(cartListString)){
            cartListString="[]";
        }
        List<CartVo> cartVoList_cookie = JSON.parseArray(cartListString, CartVo.class);


        if (name.equals("anonymousUser")){//未登录

            return cartVoList_cookie;
        }else { //已登录
            List<CartVo> cartList_redis = cartService.findCartListFromRedis(name);

            //合并购物车
            List<CartVo> cartVoList = cartService.mergeCartList(cartVoList_cookie, cartList_redis);

            if (cartVoList_cookie.size()>0){
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(name,cartVoList);
                //清除本地cookie
                CookieUtil.deleteCookie(request,response,"cartList");
                return cartVoList;
            }
            return cartList_redis;

        }


    }

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(name+"=========================================");

        try {
            //从cookie中提取购物车
            List<CartVo> cartList = findCartList();

            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (name.equals("anonymousUser")) {//未登录
                //将新的购物车存入cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"UTF-8");
                return new Result(true,"cookie 存入成功");

            }else {
                cartService.saveCartListToRedis(name,cartList);
                return new Result(true,"reids 存入成功");
            }


        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"存入失败");
        }
    }

}
