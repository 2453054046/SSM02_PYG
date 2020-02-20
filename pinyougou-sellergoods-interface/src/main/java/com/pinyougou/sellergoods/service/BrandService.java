package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.E3Result;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService {

    List<TbBrand> findAll();

    PageResult findPage(int page, int size);

    E3Result add(TbBrand tbBrand);

    TbBrand findOne(Long id);

    E3Result update(TbBrand tbBrand);

    E3Result delete(Long[] ids);

    PageResult findPage(TbBrand tbBrand, int page, int size);

    List<Map> selectOptionList();

}
