package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Dto.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteDish(@RequestParam List<Long> ids){
        dishService.removeDish(ids);
        return R.success("删除成功");
    }

}


