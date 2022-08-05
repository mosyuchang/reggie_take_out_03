package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.SetmealDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Chang
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> pageR(int page, int pageSize, String name){
        //分页构造器
        Page<Setmeal> page0 = new Page<>(page,pageSize);
        Page<SetmealDto> page1 = new Page<>(page,pageSize);
        //模糊查询
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Setmeal::getName, name);
        //排序
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //是可以把querywrapper传进去的
        setmealService.page(page0,lambdaQueryWrapper);

        //把page1里面的数据给page2，除了records，因为这个数据的范型不一样,
        // List<record>是一个行表格，包含很多信息的。针对对象进行信息展示
        BeanUtils.copyProperties(page0,page1,"records");

        List<Setmeal> list0 = page0.getRecords();

        List<SetmealDto> list1 = list0.stream().map((item) -> {
            //创建SetmealDto对象
            SetmealDto setmealDto = new SetmealDto();
            //复制item（其实就是setmeal对象）的值到seteamlDto
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);

            //判断是否为空
            if(category != null){
                setmealDto.setCategoryName(category.getName());

            }
            //需要返回的结果setmealDto对象
            return setmealDto;

        }).collect(Collectors.toList());

        page1.setRecords(list1);

        return R.success(page1);

    }

    /**
     * 批量删除方法，接受参数为list型
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功") ;
    }

    /**
     * 批量或者单一停售功能
     * @param
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> stopSelling(@PathVariable String status, @RequestParam List<Long> ids) {
        log.info("套餐id是:{},状态码事：{}",status,ids);

        setmealService.setmealEnableOrDisable(status,ids);
        //新方法
//        int statusInt = Integer.parseInt(status);
//        ids.stream().map((item) ->{
//            Setmeal setmeal = setmealService.getById(item);
//            setmeal.setStatus(statusInt);
//            return setmeal;
//        }).collect(Collectors.toList());

//

        return R.success("更改成功");
    }

//    /**
//     * 移动端页面展示套餐信息
//     * @param categoryId
//     * @param status
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(@RequestParam Long categoryId, @RequestParam String status){
//        log.info("套餐id是：{}",categoryId,status);
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
//        queryWrapper.eq(Dish::getCategoryId,categoryId);
//
//        List<Dish> list= dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

}
