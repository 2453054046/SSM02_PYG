package com.pinyougou.manager;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    //货物基本信息
	@Reference
	private GoodsService goodsService;

	//商品搜素
	@Reference(timeout = 100000)
    private ItemSearchService itemSearchService;

	//商品静态模板
    @Reference(timeout = 40000)
    private ItemPageService itemPageService;

    //消息队列工具
    @Autowired
    JmsTemplate jmsTemplate;
    //消息点对点发送添加索引库内容 点对点
    @Autowired
    Destination queueSolrDestination;
    //消息点对点发送删除索引库内容 点对点
    @Autowired
    Destination queueSolrDeleteDestination;
    //生成静态页面 广播
    @Autowired
    Destination topicPageDestination;
    //删除静态页面 广播
    @Autowired
    Destination topicPageDeleteDestination;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	/*@RequestMapping("/add")
	public Result add(@RequestBody TbGoods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}*/
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引库中删除
            //itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(queueSolrDeleteDestination, ((session)->session.createObjectMessage(ids)));


            //删除静态网页
            jmsTemplate.send(topicPageDeleteDestination, ((session)->session.createObjectMessage(ids)));
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

    /**
     * 商品审核
     * @param ids
     * @param status
     * @return
     */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long [] ids,String status){
	    try {
            goodsService.updateStatus(ids,status);

            if("1".equals(status)){
                //得到需要导入的数据
                List<TbItem> itemList = goodsService.findItemListByGoodsIdListAndStatus(ids, status);
                //导入到sorl
                //itemSearchService.importList(itemList);
                String jsonString = JSON.toJSONString(itemList);
                //使用Lamdba表达式
                jmsTemplate.send(queueSolrDestination, ((session)->session.createTextMessage(jsonString)));

                //生成静态模板
                for(Long goodsId:ids){
                    //itemPageService.getItemHtml(goodsId);
                    jmsTemplate.send(topicPageDestination, ((session)->session.createTextMessage(goodsId+"")));
                }
            }

            return new Result(true,"成功");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("e.getMessage="+e.getMessage());
            System.out.println("e="+e);
            return new Result(false,"失败");
        }
    }

    @RequestMapping("/getHtml")
    public void getHtml(Long goodsId){
        itemPageService.getItemHtml(goodsId);
    }

}
