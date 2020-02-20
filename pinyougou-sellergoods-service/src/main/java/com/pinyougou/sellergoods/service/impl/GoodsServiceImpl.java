package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    //商品
	@Autowired
	private TbGoodsMapper goodsMapper;
    //商品扩展详情
	@Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;
	//商品分类
	@Autowired
    private TbItemCatMapper tbItemCatMapper;
	//品牌
	@Autowired
    private TbBrandMapper tbBrandMapper;
	//商家
    @Autowired
    private TbSellerMapper tbSellerMapper;
    //商品详情
    @Autowired
    private TbItemMapper tbItemMapper;
	
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

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
	    //货物表
	    goods.getGoods().setAuditStatus("0");
	    goodsMapper.insert(goods.getGoods());
	    //货物扩展表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//将商品基本表的ID给商品扩展表
        tbGoodsDescMapper.insert(goods.getGoodsDesc());
        insertItemList(goods);
	}
    //向item表插入数据判断是否启用规格
    private void insertItemList(Goods goods){
        if("1".equals(goods.getGoods().getIsEnableSpec())){
            //遍历商品详情向商品详情添加
            for(TbItem item:goods.getItemList()){
                //后见标题 SPU名称+回个选项值
                String title = goods.getGoods().getGoodsName();//SPU名称
                Map<String,Object> map = JSON.parseObject(item.getSpec());//获得所有规格数据
                for(String key:map.keySet()){
                    title+=" "+map.get(key);
                }
                item.setStatus("1");
                item.setTitle(title);
                item.setSellerId(goods.getGoods().getSellerId());
                setItemValues(item,goods);
                tbItemMapper.insert(item);
            }
        }else {
            //没有启用规格
            TbItem item=new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//标题
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setNum(99999);//库存数量
            item.setStatus("1");//状态
            item.setIsDefault("1");//默认
            item.setSpec("{}");//规格
            setItemValues(item,goods);
            tbItemMapper.insert(item);

        }
    }
	//item表插入数据公共代码
    private void setItemValues(TbItem item,Goods goods){
        //商品分类获得
        item.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//更新日期

        item.setGoodsId(goods.getGoods().getId());//商品ID
        item.setSeller(goods.getGoods().getSellerId());//商家ID

        //图片
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if(imageList.size()>0){
            item.setImage((String) imageList.get(0).get("url"));
        }

        //商品分类
        TbItemCat tbItemCat = tbItemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(tbItemCat.getName());
        //品牌名称
        TbBrand tbBrand = tbBrandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(tbBrand.getName());
        //商家名称
        TbSeller tbSeller = tbSellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(tbSeller.getNickName());
    }

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
	    //更新基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新扩展表数据
        tbGoodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

        //删除原有的SKU列表数据（item表的数据）
        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdEqualTo(goods.getGoods().getId());
        tbItemMapper.deleteByExample(example);
        //插入新的SKU列表数据
        insertItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);

        //读取SKU列表
        TbItemExample example=new TbItemExample();
        example.createCriteria().andGoodsIdEqualTo(id);
        List<TbItem> itemList = tbItemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;//goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //1表示删除
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
	}
	
	
	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		//指定条件未删除的查询出来
        criteria.andIsDeleteIsNull();
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
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

    /**
     * 商品审核
     * @param ids       批量修改的商品id
     * @param status    修改的状态
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for(Long id:ids){
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    /**
     * 根据SPU的ID集合查询SKU列表
     * @param goodsIds
     * @param status
     * @return
     */
    @Override
    public List<TbItem>	findItemListByGoodsIdListAndStatus(Long []goodsIds,String status){

        TbItemExample example=new TbItemExample();
        com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
        //criteria.andStatusEqualTo(status);//状态
        criteria.andGoodsIdIn( Arrays.asList(goodsIds));//指定条件：SPUID集合
        return tbItemMapper.selectByExample(example);
    }


    /**
     * 商品上下架
     * @param status
     */
    @Override
    public void isMarketable(Long [] ids,String status) {
        for(Long id:ids){
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsMarketable(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

}
