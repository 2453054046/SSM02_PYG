package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
    RedisTemplate redisTemplate;
	//雪花随机数
	@Autowired
    IdWorker idWorker;
	@Autowired
    TbSeckillGoodsMapper tbSeckillGoodsMapper;
    /**
     * 秒杀商品订单提交缓存
     * @param seckillId
     * @param userId
     */
    @Override
    public void submitOrder(Long seckillId, String userId) {
        //获得缓存中的商品
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        if(seckillGoods==null){
            throw new RuntimeException("商品不存在");
        }
        if(seckillGoods.getStockCount()<=0){
            throw new RuntimeException("商品已被抢光");
        }
        //减少库存
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
        redisTemplate.boundHashOps("seckillGoods").put(seckillId,seckillGoods);
        if(seckillGoods.getStockCount()==0){
            tbSeckillGoodsMapper.updateByPrimaryKey(seckillGoods);//更新数据库
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
            System.out.println("向数据库同步。。。");
        }
        //存储秒杀订单
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(userId);
        seckillOrder.setSellerId(seckillGoods.getSellerId());//商家ID
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");//状态
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
        System.out.println("保存订单成功(redis)");
    }

    /**
     * 根据用户id查询缓存中秒杀订单
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    /**
     * 用户支付查询缓存保存到数据库
     * @param userId        用户id
     * @param orderId       缓存订单id
     * @param transactionId 微信支付返回的id
     */
    @Override
    public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
        //从缓存中提取商品
        TbSeckillOrder order = searchOrderFromRedisByUserId(userId);
        if(order==null){
            throw new RuntimeException("不存在订单");
        }
        if(order.getId().longValue()!=orderId.longValue()){
            throw new RuntimeException("订单号不符");
        }

        //修改订单属性
        order.setPayTime(new Date());
        order.setStatus("1");//已付款
        order.setTransactionId(transactionId);

        //将订单存入数据库
        seckillOrderMapper.insert(order);
        //清除对应缓存
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }

    /**
     * 订单超时回退
     * @param userId        用户id
     * @param orderId       订单id
     */
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //查询缓存中的订单
        TbSeckillOrder order = searchOrderFromRedisByUserId(userId);
        if(order!=null){
            //删除缓存中的订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
            //秒杀缓存库存回退
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(order.getSellerId());
            if(seckillGoods!=null){ //如果商品还存在
                seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getSellerId(),seckillGoods);
            }else { //商品不存在，向缓存重新加入该商品继续秒杀
                seckillGoods=new TbSeckillGoods();
                seckillGoods.setId(order.getSeckillId());
                //属性要设置。。。。省略
                seckillGoods.setStockCount(1);//数量为1
                redisTemplate.boundHashOps("seckillGoods").put(order.getSeckillId(), seckillGoods);
            }
            System.out.println("订单取消："+orderId);
        }
    }


}
