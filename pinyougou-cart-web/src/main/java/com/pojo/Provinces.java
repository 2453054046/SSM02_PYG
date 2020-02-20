package com.pojo;

import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;

import java.io.Serializable;
import java.util.List;

public class Provinces implements Serializable {

    private List<TbAreas> areasList;

    private List<TbCities> citiesList;

    private List<TbProvinces> provincesList;


    public List<TbAreas> getAreasList() {
        return areasList;
    }

    public void setAreasList(List<TbAreas> areasList) {
        this.areasList = areasList;
    }

    public List<TbCities> getCitiesList() {
        return citiesList;
    }

    public void setCitiesList(List<TbCities> citiesList) {
        this.citiesList = citiesList;
    }

    public List<TbProvinces> getProvincesList() {
        return provincesList;
    }

    public void setProvincesList(List<TbProvinces> provincesList) {
        this.provincesList = provincesList;
    }
}
