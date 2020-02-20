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
//设置dubbo的过时时间
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    SolrTemplate solrTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 搜索
     *
     * 搜索对象的结构  keywords:搜索关键字 category：商品分类  brand：品牌  spec：规格 price:价格
     */
    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap<>();
        //空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ","")); //搜索关键字去掉空格

        //高亮查询列表
        map.putAll(searchList(searchMap));

        //分组查询商品分类列表
        List<String> categoryList = searchCatgoryList(searchMap);
        map.put("categoryList",categoryList);

        //3.查询品牌和规格列表
        String category= (String) searchMap.get("category");
        if(!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));
        }else{
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    /**
     * 导入审核的列表到solr
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 根据多个SPU删除商品
     * @param goodsIds
     */
    @Override
    public void deleteByGoodsIds(List goodsIds) {

        SolrDataQuery query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //高亮查询列表
    private Map searchList(Map searchMap){




        Map map = new HashMap<>();
       /* Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);
        //tbItems.getContent()搜索的结果
        map.put("rows",tbItems.getContent());*/

        /*==========高亮==========*/
        //设置高亮查询
        HighlightQuery query = new SimpleHighlightQuery();
        //构建高亮选项对象
        HighlightOptions highlight = new HighlightOptions().addField("item_title");//设置高亮域
        highlight.setSimplePrefix("<em style='color:red'>");        //设置前缀
        highlight.setSimplePostfix("</em>");                        //后缀
        query.setHighlightOptions(highlight);                       //为查询对象设置高亮选项


        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //按照商品分类过滤
        if(!"".equals(searchMap.get("category"))){ //如果用户选择了分类
            FilterQuery filterQuery = new SimpleFacetQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category")); //过滤的域
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);//添加过滤条件
        }

        //按照品牌过滤
        if(!"".equals(searchMap.get("brand"))){ //如果用户选择了分类
            FilterQuery filterQuery = new SimpleFacetQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));  //过滤的域对应的值
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);//添加过滤条件
        }


        //1.4 按规格过滤
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map<String, String>) searchMap.get("spec");
            for(String key :specMap.keySet()){

                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key)  );
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }

        }

        //价格过滤
        if(!"".equals(searchMap.get("price"))){
            String[] price = ((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){//最低价格不等于0
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);//大于等于
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }
            if(!price[1].equals("*")){//最高价格不等于0
                FilterQuery filterQuery=new SimpleFilterQuery();
                Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);//小于等于
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);

            }
        }

        //分页
        Integer pageNo =(Integer) searchMap.get("pageNo");//获得页码
        if(pageNo==null){
            pageNo=1;
        }
        Integer pageSize =(Integer) searchMap.get("pageSize");//获得页码
        if(pageSize==null){
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);//起始索引
        query.setRows(pageSize);//每页记录


        //排序
        String sort= (String) searchMap.get("sort");//标记升降序
        String sortField = (String) searchMap.get("sortField");//排序的域
        if(sort!=null && ! sort.equals("")){
            if(sort.equals("ASC")){
                Sort item_sortField = new Sort(Sort.Direction.ASC, "item_"+sortField);
                query.addSort(item_sortField);
            }
            if(sort.equals("DESC")){
                Sort item_sortField = new Sort(Sort.Direction.DESC, "item_"+sortField);
                query.addSort(item_sortField);
            }
        }




        /*==========  获取高亮结果集  ===========*/
        //使用高亮对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for(HighlightEntry<TbItem> entry:entryList){
            //获取高亮列表(高亮域的个数)
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
            /*for(HighlightEntry.Highlight h:highlights){
                List<String> snipplets = h.getSnipplets();//每个域有可能存在多个值
                System.out.println(snipplets);//高亮结果
            }*/
            //判断搜索结果是否有值
            if(highlights.size()>0 && highlights.get(0).getSnipplets().size()>0){
                TbItem entity = entry.getEntity();
                //将高亮结果放入返回值
                entity.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//总页数
        map.put("total",page.getTotalElements());//总记录数
        return map;
    }
    /**
     * 分组查询（查询商品分类列表）
     * @return
     */
    private List<String> searchCatgoryList(Map searchMap){
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery("*:*");
        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords")); //where
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOption=new GroupOptions().addGroupByField("item_category");//设置分组的域 group by
        query.setGroupOptions(groupOption);
        //获得分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");//查询分组的域
        //获得分组入口
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:entryList){
            list.add(entry.getGroupValue());//向返回结果添加分组查询的结果
        }
        return list;
    }

    /**
     * 根据商品分类名称查询品牌和规格
     * @param category  商品分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
      /*  Map map = new HashMap<>();
        //根据商品分类名名称得到模板ID
        Long tempId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(tempId!=null){
            //根据模板ID获得品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(tempId);
            map.put("brandList",brandList);
            //根据模板ID获得规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(tempId);
            map.put("specList",specList);
        }*/

        Map map=new HashMap();
        //1.根据商品分类名称得到模板ID
        Long templateId= (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if(templateId!=null){
            //2.根据模板ID获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            System.out.println("品牌列表条数："+brandList.size());

            //3.根据模板ID获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
            System.out.println("规格列表条数："+specList.size());
        }

        return map;
    }
}
