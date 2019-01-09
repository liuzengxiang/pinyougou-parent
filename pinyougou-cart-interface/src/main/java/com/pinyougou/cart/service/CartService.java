package com.pinyougou.cart.service;


import vo.CartVo;

import java.util.List;

//购物车服务接口
public interface CartService {

    //添加商品到购物车列表
    public List<CartVo> addGoodsToCartList(List<CartVo> list,Long itemId,Integer num);

    //从redis中提取购物车
    public List<CartVo> findCartListFromRedis(String username);

    //将购物车列表存入redis
    public void saveCartListToRedis(String username,List<CartVo> cartVoList);

    //合并购物车
    public List<CartVo> mergeCartList(List<CartVo> cartVoList1,List<CartVo> cartVoList2);
}
