package com.pinyougou.page.service.impl;


import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${pagedir}")
    String pagedir;
    //商品基本表
    @Autowired
    TbGoodsMapper tbGoodsMapper;
    //商品扩展表
    @Autowired
    TbGoodsDescMapper tbGoodsDescMapper;
    //商品分类
    @Autowired
    TbItemCatMapper tbItemCatMapper;

    @Autowired
    TbItemMapper tbItemMapper;
    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    @Override
    public boolean getItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();

        try {
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模板
            Map map = new HashMap<>();
            //获得基本表数据
            TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods",tbGoods);
            //获得扩展表数据
            TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",tbGoodsDesc);
            //获得商品分类
            String name1 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            String name2 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            String name3 = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            map.put("itemCat1",name1);
            map.put("itemCat2",name2);
            map.put("itemCat3",name3);
            //读取SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");//按字段排序，目的返回结果是第一条默认SKU
            List<TbItem> tbItems = tbItemMapper.selectByExample(example);
            map.put("itemList",tbItems);
            //输出位置
            FileWriter out = new FileWriter(pagedir + goodsId + ".html");
            //输出
            template.process(map,out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量删除静态页面
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long id:goodsIds)
                new File(pagedir + id + ".html").delete();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
