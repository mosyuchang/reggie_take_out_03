package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;



    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping()
    public R<String> save(@RequestBody DishDto dishDto) {
        //输出验证一下
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");

    }

    /**
     * 菜品页面分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page (int page, int pageSize, String name){
        //构造分页构造器
        Page<Dish> dishPage = new Page<>(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Dish>  lambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        lambdaQueryWrapper.like(name != null, Dish::getName,name);

        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(dishPage,lambdaQueryWrapper);

        //建立dto范型，因为原来的dish范型里面没有菜品分类的值，dto里面包含了
        Page<DishDto> dishDtoPage = new Page<>();

        //对应拷贝
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");


        List<Dish> records = dishPage.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            //创建dishdto对象
            DishDto dishDto = new DishDto();
            //将dish中的属性值赋给dishdto
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;


        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     *根据菜品id查询菜品信息和口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}") // 请求在url里面
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getWithFlavor(id);

        return R.success(dishDto);

    }

    /**
     * 保存
     * @param dishDto
     * @return
     */
    @PutMapping
    public  R<String>  updateSave(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return  R.success("修改菜品成功");
    }

//    /**
//     * 根据条件查询具体数据(旧方法)
//     * 传进来的时 category id，用long去接受是可以的，但是用dish对象接受更有通用性
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<DishDto>> list(Dish dish){
//        //构造条件查询器
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(dish.getCategoryId()!= null,Dish::getCategoryId, dish.getCategoryId());
//        //只查询状态为1的菜品
//        lambdaQueryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(lambdaQueryWrapper);
//
//        return R.success(list);
//
//
//    }

    /**
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId,dish.getCategoryId());
        //查出状态为1的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //按照sort升序，update时间降序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //把dish转成dishdto
            //先把dish里面的list集合取出来
            List<Dish> dishList = dishService.list(queryWrapper);

            List<DishDto> dishDtoList = dishList.stream().map((item) ->{
                DishDto dishDto = new DishDto();
                //1.把dish对象里面的值赋给dishdto
                BeanUtils.copyProperties(item,dishDto);
                //2.得到categoryid.通过categoryservice得到对象
                Category category = categoryService.getById(item.getCategoryId());
                //不为空就得到categoryname
                if(category != null){
                    String categoryName = category.getName();
                    dishDto.setCategoryName(categoryName);
                }
                //3.得到dishflavor.根据dish对象得到dishid。但是一个dishid可以对应着
                // 多个id（一道菜有着不同的口味忌口），所以得到的是一个list
                Long dishId = item.getId();
                LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
                //lambdaquerywrapper得到的是一个结果集
                lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
                //得到一个以dishflavor对象为元素的集合；
                //list方法是用来把这些子集封装成一个list
                //这步就是捆绑包装操作
                List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
                if(dishFlavorList != null){
                    dishDto.setFlavors(dishFlavorList);
                }

                return dishDto;

            }).collect(Collectors.toList());



        return R.success(dishDtoList);
    }

    /**
     * 批量启售或停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> dishEnableOrDisable(@PathVariable String status, @RequestParam List<Long> ids){
        log.info("ids是：{}",ids);
        dishService.dishEnableOrDisable(status,ids);
        return R.success("更改成功");
    }

    /**
     *删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids){

        //根据菜品id在stemeal_dish表中查出哪些套餐包含该菜品
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getDishId,ids);
        List<SetmealDish> SetmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);
        //如果菜品没有关联套餐，直接删除就行  其实下面这个逻辑可以抽离出来，这里我就不抽离了
        if (SetmealDishList.size() == 0){
            //这个deleteByIds中已经做了菜品起售不能删除的判断力
            dishService.removeDish(ids);
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DishFlavor::getDishId,ids);
            dishFlavorService.remove(queryWrapper);
            return R.success("菜品删除成功");
        }

        //如果菜品有关联套餐，并且该套餐正在售卖，那么不能删除
        //得到与删除菜品关联的套餐id
        ArrayList<Long> Setmeal_idList = new ArrayList<>();
        for (SetmealDish setmealDish : SetmealDishList) {
            Long setmealId = setmealDish.getSetmealId();
            Setmeal_idList.add(setmealId);
        }
        //查询出与删除菜品相关联的套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,Setmeal_idList);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        //对拿到的所有套餐进行遍历，然后拿到套餐的售卖状态，如果有套餐正在售卖那么删除失败
        for (Setmeal setmeal : setmealList) {
            Integer status = setmeal.getStatus();
            if (status == 1){
                return R.error("删除的菜品中有关联在售套餐,删除失败！");
            }
        }

        //要删除的菜品关联的套餐没有在售，可以删除
        //这下面的代码并不一定会执行,因为如果前面的for循环中出现status == 1,那么下面的代码就不会再执行
        dishService.removeDish(ids);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
        return R.success("菜品删除成功");


    }

    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param SetmealId
     * @return
     */
    //这里前端是使用路径来传值的，要注意，不然你前端的请求都接收不到，就有点尴尬哈
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        //获取套餐里面的所有菜品  这个就是SetmealDish表里面的数据
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //其实这个BeanUtils的拷贝是浅拷贝，这里要注意一下
            BeanUtils.copyProperties(setmealDish, dishDto);
            //这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }


}


