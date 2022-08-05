package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐同时保证套餐与菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐同时删除套餐与菜品的关系
     * @param ids
     */
    public  void removeWithDish(List<Long> ids);

    void setmealEnableOrDisable(String status, List<Long> ids);
}
