package test;

import entity.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-solr.xml")
public class testSorl {

    @Autowired
    private SolrTemplate solrTemplate;

    @Test
    public void addSorl(){
        TbItem tbItem = new TbItem();
        tbItem.setId(1L);
        tbItem.setTitle("华为");
        tbItem.setCategory("手机");
        tbItem.setBrand("华为");
        tbItem.setSeller("华为旗舰店");
        tbItem.setGoodsId(1L);
        tbItem.setPrice(new BigDecimal(3000.01));

        solrTemplate.saveBean(tbItem);
        solrTemplate.commit();
    }

    //按主键查询
    @Test
    public void testFindOne(){
        TbItem item = solrTemplate.getById(1, TbItem.class);
        System.out.println(item.getTitle());
    }

    //按主键删除
    @Test
    public void testDelete(){
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }

    //批量导入
    @Test
    public void testAddList(){
        List<TbItem> list=new ArrayList();

        for(int i=0;i<100;i++){
            TbItem item=new TbItem();
            item.setId(i+1L);
            item.setBrand("华为");
            item.setCategory("手机");
            item.setGoodsId(1L);
            item.setSeller("华为2号专卖店");
            item.setTitle("华为Mate"+i);
            item.setPrice(new BigDecimal(2000+i));
            list.add(item);
        }
        //向solr索引库导入集合
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    @Test
    public void testPageQuery(){
        Query query=new SimpleQuery("*:*");
        query.setOffset(20);//开始索引（默认0）
        query.setRows(20);//每页记录数(默认10)
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        for(TbItem item:page.getContent()){
            System.out.println(item.getTitle()+" "+item.getPrice()+" "+item.getBrand());
        }
        System.out.println("总记录数："+page.getTotalElements());
        System.out.println("总页数："+page.getTotalPages());
    }
    //条件查询
    @Test
    public void testPageQueryMutil(){
        Query query=new SimpleQuery("*:*");
        //设置条件  contains：包含  is：等于
        Criteria criteria=new Criteria("item_category").contains("手机");
        //追加条件
        criteria=criteria.and("item_brand").contains("2");
        query.addCriteria(criteria);
        //query.setOffset(20);//开始索引（默认0）
        //query.setRows(20);//每页记录数(默认10)
        //开始查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query,TbItem.class);
        //page.getContent()查询的结果
        for(TbItem item:page.getContent()){
            System.out.println(item.getTitle()+" "+item.getPrice()+" "+item.getBrand());
        }
        System.out.println("总记录数："+page.getTotalElements());
        System.out.println("总页数："+page.getTotalPages());

    }
    //删除全部
    @Test
    public void testDeleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }



}
