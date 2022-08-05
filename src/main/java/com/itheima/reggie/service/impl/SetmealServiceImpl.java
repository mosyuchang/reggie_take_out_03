package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {
    @Autowired
    private  SetmealDishService setmealDishService;

    @Autowired
    private  SetmealService setmealService;

    public SetmealServiceImpl(SetmealDishService setmealDishService) {
        this.setmealDishService = setmealDishService;
    }

    /**
     * 新增套餐同时保证套餐与菜品的关联关系
     * @param setmealDto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal表，执行insert操作
        this.save(setmealDto);

        //保存套餐和菜品的关联信息setmeal_dish，执行insert
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        List<SetmealDish> list = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     *这个方法是用来删除包含关系表的数据
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWithDish(List<Long> ids) {
        //
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // select count(*) from setmeal where id = {1,2,3} and status = 1
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);
        //判断是否菜品被禁售了，否则返回customexception
        int count = this.count(lambdaQueryWrapper);
        if(count >0){
            throw new CustomException("套餐仍然在售卖");
        }
        //删除套餐表中的数据
        this.removeByIds(ids);
        //删除关系表中的数据,此时的ids并不是关系表里面的主键，需要用lambdaquerywrapper
        //select * from setmeal_dish where setmealid in ids;
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(setmealDishLambdaQueryWrapper);



        //

    }

    @Override
    public void setmealEnableOrDisable(String status, List<Long> ids) {
        UpdateWrapper<Setmeal> updateWrapper = new UpdateWrapper<>();
        if("1".equals(status)){
            updateWrapper.set(ids != null,"status",1).in("id",ids);
            this.update(updateWrapper);
        }else {
            updateWrapper.set(ids != null,"status",0).in("id",ids);
            this.update(updateWrapper);
        }

    }

}
