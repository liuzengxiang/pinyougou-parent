package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import vo.GoodsVo;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}



	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(GoodsVo goodsVo) {
		goodsVo.getGoods().setAuditStatus("0");
		goodsMapper.insert(goodsVo.getGoods());

		goodsVo.getGoodsDesc().setGoodsId(goodsVo.getGoods().getId());
		goodsDescMapper.insert(goodsVo.getGoodsDesc());
		saveItemList(goodsVo);

	}

	private void saveItemList(GoodsVo goodsVo){
		if ("1".equals(goodsVo.getGoods().getIsEnableSpec())) {
			for (TbItem item : goodsVo.getItemList()) {
				String title = goodsVo.getGoods().getGoodsName(); //SPU 名称
				Map<String,Object> map =JSON.parseObject(item.getSpec());
				for (String key : map.keySet()) {
					title += " "+map.get(key);
				}
				item.setTitle(title);

				setItemValues(item,goodsVo);
				itemMapper.insert(item);
			}
		}else {
			TbItem item = new TbItem();
			item.setTitle(goodsVo.getGoods().getGoodsName());
			item.setPrice(goodsVo.getGoods().getPrice());
			item.setNum(99999);
			item.setStatus("1");
			setItemValues(item,goodsVo);
			itemMapper.insert(item);
		}
	}


	private void setItemValues(TbItem item,GoodsVo goodsVo){
		item.setCategoryid(goodsVo.getGoods().getCategory3Id());
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//更新日期
		item.setGoodsId(goodsVo.getGoods().getId());//商品ID
		item.setSellerId(goodsVo.getGoods().getSellerId());// 商家ID

		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());

		TbBrand brand = brandMapper.selectByPrimaryKey(goodsVo.getGoods().getBrandId());
		item.setBrand(brand.getName());

		TbSeller seller = sellerMapper.selectByPrimaryKey(goodsVo.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		List<Map> imageList = JSON.parseArray(goodsVo.getGoodsDesc().getItemImages(), Map.class);
		if (imageList!=null &&imageList.size()>0){
			item.setImage((String) imageList.get(0).get("url"));
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(GoodsVo goodsVo){
		goodsMapper.updateByPrimaryKey(goodsVo.getGoods());
		goodsDescMapper.updateByPrimaryKey(goodsVo.getGoodsDesc());

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goodsVo.getGoods().getBrandId());
		itemMapper.deleteByExample(example);

		saveItemList(goodsVo);

	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public GoodsVo findOne(Long id){
		GoodsVo goodsVo = new GoodsVo();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goodsVo.setGoods(tbGoods);
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goodsVo.setGoodsDesc(tbGoodsDesc);

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> tbItems = itemMapper.selectByExample(example);
		goodsVo.setItemList(tbItems);

		return goodsVo;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
//			goodsMapper.deleteByPrimaryKey(id);
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}


	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();

		if(goods!=null){
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}

		}

		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateGoodsStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}

	//根据SPU的ID查询SKU的列表
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds,String status){

		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);//状态
		criteria.andGoodsIdIn( Arrays.asList(goodsIds));//指定条件：SPUID集合

		List<TbItem> tbItems = itemMapper.selectByExample(example);
		return tbItems;

	}
}
