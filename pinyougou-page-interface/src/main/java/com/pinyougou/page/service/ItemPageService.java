package com.pinyougou.page.service;

public interface ItemPageService {
    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean getItemHtml(Long goodsId);

    /**
     * 批量删除静态模板
     * @param goodsIds
     * @return
     */
    boolean deleteItemHtml(Long[] goodsIds);
}
