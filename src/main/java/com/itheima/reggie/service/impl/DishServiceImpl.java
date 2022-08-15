package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chang
 */
@Service
@Slf4j

public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增菜品同时保存对应的口味数据
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息进dish表
        this.save(dishDto);

        //因为 在返回来的dto中是两张表的数据整合，他们有外键链接是菜品id dishID，
        // 所以在把剩余的数据给dishFlavor时会缺少dishID。我们需要手动给加上
        //菜品id
        Long dishID = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //保存菜品口味到dishFlavor表,用流的写法
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishID);
            return  item;
        }).collect(Collectors.toList());
        //dishFlavorService.saveBatch();


    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DishDto getWithFlavor(Long id) {
        //根据id查询菜品信息，查出一个对象
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
            //将除了风味之外的信息赋给dishDto
        BeanUtils.copyProperties(dish,dishDto);
            //查询当前菜品对应的口味信息
            //构造条件查询器
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询第二章表
            //根据dish的id查出dishFlavor对象
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
            //把对象中的flavor属性拿出来
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
            //注入dishDTO对象中

        dishDto.setFlavors(list);

        //返回包含所有信息的dishDTO对象
        return dishDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWithFlavor(DishDto dishDto) {
        //第一步先把dishDto中的信息存进dish表
        this.updateById(dishDto);


        //第二部把剩余信息存进风味表（flavor）
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
            //清理当前菜品对应的口味数据
        dishFlavorService.remove(lambdaQueryWrapper);
            //重新添加
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return  item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);


    }

    /**
     * 批量开启或停售菜品
     * @param status
     * @param ids
     */
    @Override
    public void dishEnableOrDisable(String status, List<Long> ids) {

        //判断菜品是否在套餐中销售

        //更改状态
        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        if("1".equals(status)){
            updateWrapper.set(ids != null,"status",1).in("id",ids);
            this.update(updateWrapper);
        }else {
            updateWrapper.set(ids != null,"status",0).in("id",ids);
            this.update(updateWrapper);
        }
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    public void removeDish(List<Long> ids) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId,ids);
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        int count = this.count(lambdaQueryWrapper);
        //判断了十分偶未被停售
        if(count > 0){
            throw new CustomException("菜品仍在售卖");
        }
        //删除菜品表中的菜品
        this.removeByIds(ids);







    }


}
