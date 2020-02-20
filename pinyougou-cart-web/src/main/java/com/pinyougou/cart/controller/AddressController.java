package com.pinyougou.cart.controller;
import java.util.List;

import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import com.pojo.Provinces;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/address")
public class AddressController {

	@Reference(timeout = 20000)
	private AddressService addressService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbAddress> findAll(){			
		return addressService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return addressService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param address
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbAddress address){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        address.setUserId(username);
        try {
			addressService.add(address);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param address
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbAddress address){
		try {
			addressService.update(address);
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
	public TbAddress findOne(Long id){
		return addressService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param id
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long id){
		try {
			addressService.delete(id);
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
	public PageResult search(@RequestBody TbAddress address, int page, int rows  ){
		return addressService.findPage(address, page, rows);		
	}

    /**
     * 获得登陆用户的收货地址
     * @return
     */
	@RequestMapping("/findListByLoginUser")
    public List<TbAddress> findListByLoginUser(){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<TbAddress> listByUserId = addressService.findListByUserId(userId);
        return listByUserId;
    }

    /**
     * 获得省份
     * @return
     */
    @RequestMapping("fingProvincesList")
    public Provinces fingProvincesList(){
        List<TbProvinces> list = addressService.fingProvincesList();
        Provinces provinces = new Provinces();
        provinces.setProvincesList(list);
        return provinces;
    }

    /**
     * 获得市区
     * @return
     */
    @RequestMapping("findcitiesList")
    public Provinces findcitiesList(String id){
        List<TbCities> cities=addressService.findcitiesList(id);
        Provinces provinces = new Provinces();
        provinces.setCitiesList(cities);
        return provinces;
    }

    /**
     * 获得县
     * @return
     */
    @RequestMapping("findareasList")
    public Provinces findareasList(String id){
        List<TbAreas> areas = addressService.findareasList(id);
        Provinces provinces = new Provinces();
        provinces.setAreasList(areas);
        return provinces;
    }
}
