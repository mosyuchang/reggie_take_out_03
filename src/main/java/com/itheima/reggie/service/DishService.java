package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

/**
 * @author Chang
 */
public interface DishService extends IService<Dish> {
    //新增菜品同时插入新增菜品对应的口味数据，需要同时操作两张表dish表和dishFlavor表
    public void saveWithFlavor (DishDto dishDto);

    //根据id查询菜品信息和口味信息
    public DishDto getWithFlavor (Long id);

    public void updateWithFlavor(DishDto dishDto);

    void dishEnableOrDisable(String status, List<Long> ids);

    void removeDish(List<Long> ids);
}
