package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时调度任务
 */
@Component
public class SeckillTask {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 定时向缓存增加秒杀商品
     */
    @Scheduled(cron = "0 * * * * ?")//秒 分 时 周 ？ 年   每分钟执行一次
    public void refreshSeckillGoods() {
        System.out.println("执行了秒杀商品增量更新 任务调度"+new Date());
        //获得缓存中所有的秒杀商品的key
        List goodsIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");// 审核通过的商品
        criteria.andStockCountGreaterThan(0);//库存数大于0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始日期小于等于当前日期
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());//截止日期大于等于当前日期
        if(goodsIdList.size()>0){//排除缓存中存在的秒杀商品
            criteria.andIdNotIn(goodsIdList);
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        for(TbSeckillGoods seckillGoods:seckillGoodsList){
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
            System.out.println("增量更新秒杀商品ID:"+seckillGoods.getId());
        }
        System.out.println(".....end....");
    }

    /**
     * 定时删除缓存的秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")//每秒执行
    public void removeSeckillGoods(){
        //获得所有缓存商品
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        for(TbSeckillGoods seckillGoods:seckillGoodsList){
            if(seckillGoods.getEndTime().getTime()<new Date().getTime()){
                //同步数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                //清除缓存
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
            }
        }
        System.out.println("执行定时清除秒杀任务。。。end");
    }
}