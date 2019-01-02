package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService{

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {

       /* Query query = new SimpleQuery("*:*");
        if (searchMap.get("keywords")!=null && !"".equals(searchMap.get("keywords"))) {

            Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
            query.addCriteria(criteria);
        }
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows",page.getContent());*/

       Map map = new HashMap();

        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));

        //查询列表
       map.putAll(searchList(searchMap));

       //分组查询
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        if (categoryList != null && categoryList.size() >0) {
            //查询品牌和规格列表

            map.putAll(searchBrandAndSpecList(categoryList.get(0)));
        }


        String categoryName=(String)searchMap.get("category");
        if(!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{//如果没有分类名称，按照第一个查询
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }



        return map;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    //根据商品分类名称 查询品牌和规格列表
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        //根据商品分类名称获取模版ID
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId != null) {
            //根据模版ID获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);

            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);

        }


        return map;
    }

    //查询列表
    private Map searchList(Map searchMap){
        Map map = new HashMap();


        //高亮选项初始化
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮的字段
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        query.setHighlightOptions(highlightOptions);//为查询设置高亮选项


        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //分类过滤
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria fiterCriteria= new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(fiterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //品牌过滤
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //规格过滤
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }


        //价格过滤
        if (!"".equals(searchMap.get("price"))){
            String priceStr = (String) searchMap.get("price");
            String[] price = priceStr.split("-");
            if (!price[0].equals("0")){//起始价格不等于0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria fiterCriteria= new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(fiterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){//最高价格不等于追单价格
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria fiterCriteria= new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(fiterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //分页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo == null){//当前页
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null){//每页显示记录数
            pageSize = 20;
        }
        query.setOffset((pageNo - 1) * pageSize );//起始索引
        query.setRows(pageSize);

        
        //排序
        String sortValue = (String) searchMap.get("sort"); //升序或者降序
        String sortFieldValue = (String) searchMap.get("sortField"); //升序或者降序
        if (sortValue != null && !sortValue.equals("")) {
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortFieldValue);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortFieldValue);
                query.addSort(sort);
            }
        }

        //返回高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口的集合
        List<HighlightEntry<TbItem>> entityList = page.getHighlighted();

        for (HighlightEntry<TbItem> entity : entityList) {
            //获取高亮域(每个列)
            List<HighlightEntry.Highlight> highlights = entity.getHighlights();
            if (highlights.size()>0 && highlights.get(0).getSnipplets().size()>0) {
                TbItem item = entity.getEntity();
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
       /*     for (HighlightEntry.Highlight highlight : highlights) {
                List<String> snipplets = highlight.getSnipplets();
                System.out.println(snipplets);
            }*/
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总页数

        return map;
    }

    private List<String> searchCategoryList(Map searchMap){

        List<String> list = new ArrayList();

        Query query = new SimpleQuery("*:*");
        //关键字查询(相当于where)
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//相当于GROUP BY
        query.setGroupOptions(groupOptions);
        //获取分组夜页     一个分组也包含多个分组结果
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象     分组可以多个所以用下面方法取出某个分组的结果
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entity : content) {
            list.add(entity.getGroupValue());
        }
        return list;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    public void deleteByGoodsIds(List goodsIds){

        Query query= new SimpleQuery("*:*");

        Criteria caiteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(caiteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
