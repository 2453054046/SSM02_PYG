package com.pinyougou.manager;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import entity.E3Result;
import entity.PageResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class controller {

    @Reference
    private BrandService brandService;

    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        System.out.println("进入findAll controller");
        return brandService.findAll();
    }
    //分页查询品牌
    @RequestMapping("/pageAll")
    public PageResult pageAll(int page,int size){
        return brandService.findPage(page,size);
    }

    //添加品牌
    @RequestMapping("/add")
    public  E3Result add(@RequestBody TbBrand tbBrand){
        return  brandService.add(tbBrand);
    }

    //修改商品
    //回显商品
    @RequestMapping("/findById")
    public TbBrand findById(Long id){
        return brandService.findOne(id);
    }
    @RequestMapping("/update")
    public E3Result update(@RequestBody TbBrand tbBrand){
        return brandService.update(tbBrand);
    }

    //删除品牌
    @RequestMapping("del")
    public E3Result del(Long[] ids){
        return brandService.delete(ids);
    }

    //条件查询
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand tbBrand,int page,int size){
        return brandService.findPage(tbBrand,page,size);
    }
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList() {
        return brandService.selectOptionList();
    }
}
