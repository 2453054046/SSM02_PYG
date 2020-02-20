package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.E3Result;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/**
 * 品牌管理
 */
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    TbBrandMapper tbBrandMapper;

    //获得所有品牌
    @Override
    public List<TbBrand> findAll() {
        TbBrandExample tbBrandExample = new TbBrandExample();
        List<TbBrand> tbBrands = tbBrandMapper.selectByExample(tbBrandExample);
        return tbBrands;
    }
    //分页获得所有品牌
    @Override
    public PageResult findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<TbBrand> tbBrands = (Page<TbBrand>) tbBrandMapper.selectByExample(null);
        return new PageResult(tbBrands.getTotal(),tbBrands.getResult());
    }
    //添加品牌
    @Override
    public E3Result add(TbBrand tbBrand) {
        try {
            int insert = tbBrandMapper.insert(tbBrand);
            if(insert>0){
                return E3Result.ok();
            }
        }catch (Exception e){
            e.printStackTrace();
            return E3Result.build(400,"添加失败");
        }
        return E3Result.build(400,"添加失败");
    }

    //回显商品
    @Override
    public TbBrand findOne(Long id) {
        return tbBrandMapper.selectByPrimaryKey(id);
    }
    //修改商品
    @Override
    public E3Result update(TbBrand tbBrand) {
        try {
            int i = tbBrandMapper.updateByPrimaryKey(tbBrand);
            return E3Result.ok();
        }catch (Exception e){
            return E3Result.build(400,"修改失败");
        }
    }
    //删除
    @Override
    public E3Result delete(Long[] ids) {
        try {
            for(Long id:ids){
                tbBrandMapper.deleteByPrimaryKey(id);
            }
            return E3Result.ok();
        }catch (Exception e){
            return E3Result.build(400,"删除失败");
        }
    }
    //条件查询
    @Override
    public PageResult findPage(TbBrand tbBrand, int page, int size) {
        PageHelper.startPage(page,size);
        TbBrandExample tbBrandExample = new TbBrandExample();
        TbBrandExample.Criteria criteria = tbBrandExample.createCriteria();
        if(tbBrand !=null){
            if(!StringUtils.isEmpty(tbBrand.getName())){
                criteria.andNameLike("%"+tbBrand.getName()+"%");
            }
            if(!StringUtils.isEmpty(tbBrand.getFirstChar())){
                criteria.andFirstCharLike("%"+tbBrand.getFirstChar()+"%");
            }
        }
        Page<TbBrand> tbBrands = (Page<TbBrand>) tbBrandMapper.selectByExample(tbBrandExample);
        return new PageResult(tbBrands.getTotal(),tbBrands.getResult());
    }

    //品牌下拉列表
    @Override
    public List<Map> selectOptionList() {
        return tbBrandMapper.selectOptionList();
    }
}
