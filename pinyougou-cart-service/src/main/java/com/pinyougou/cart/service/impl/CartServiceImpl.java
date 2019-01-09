package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import vo.CartVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<CartVo> findCartListFromRedis(String username) {
        System.out.println("get for reids");
        List<CartVo> cartlist = (List<CartVo>) redisTemplate.boundHashOps("cartlist").get(username);
        if (cartlist == null){
            cartlist = new ArrayList<>();
        }
        return cartlist;
    }

    @Override
    public void saveCartListToRedis(String username, List<CartVo> cartVoList) {
        System.out.println("put to reids");
        redisTemplate.boundHashOps("cartlist").put(username,cartVoList);
    }

    @Override
    public List<CartVo> addGoodsToCartList(List<CartVo> cartVoList, Long itemId, Integer num) {
        //根据SKU的ID查询商品明细的对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态不合法");
        }

        //根据SKU对象得到商家ID
        String sellerId = item.getSellerId();

        // 根据商家ID 在购物车列表中查询购物车对象
        CartVo cartVo = searchCartBySellerId(cartVoList, sellerId);

        if (cartVo==null){  //如果购物车列表中不存在该商家的购物车
            //创建一个新的购物车对象
            cartVo = new CartVo();
            cartVo.setSellerId(sellerId);
            cartVo.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();
            //创建新的购物车明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);
            cartVo.setOrderItemList(orderItemList);
            //如果购物车列表中存在该商家的购物车

            cartVoList.add(cartVo);
        }else {   //如果购物车列表中存在该商家的购物车

            //判断该商品是否在该商品购物车明细中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cartVo.getOrderItemList(), itemId);

            if (orderItem==null){ //如果不存在创建新的明细购物车明细对象并添加
                orderItem = createOrderItem(item,num);
                cartVo.getOrderItemList().add(orderItem);
            }else {//如果存在原有的数量上添加数量，并更新金额
                //更改数量
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

                //如果明细的数量小于等于0 衣橱该明细
                if (orderItem.getNum()<=0){
                    cartVo.getOrderItemList().remove(orderItem);
                }
                if (cartVo.getOrderItemList().size()==0){
                    cartVoList.remove(cartVo);
                }
            }




        }



        return cartVoList;
    }

    private CartVo searchCartBySellerId(List<CartVo> cartVoList,String sellerId){
        for (CartVo cartVo : cartVoList) {
            if (cartVo.getSellerId().equals(sellerId));
            return cartVo;
        }
        return null;
    }

    //根据SKUid在购物车明细列表中查询购物车明细对象
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItems,Long itemId){
        for (TbOrderItem orderItem : orderItems) {
            if (orderItem.getItemId().longValue() == itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item,Integer num){
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    @Override
    public List<CartVo> mergeCartList(List<CartVo> cartVoList1, List<CartVo> cartVoList2) {
//        return cartVoList1.addAll(cartVoList2);  会出现重复数据
        for (CartVo cartVo : cartVoList2) {
            for (TbOrderItem orderItem : cartVo.getOrderItemList()) {
                cartVoList1 = addGoodsToCartList(cartVoList1,orderItem.getItemId(),orderItem.getNum());
            }
        }

        return cartVoList1;
    }
}
