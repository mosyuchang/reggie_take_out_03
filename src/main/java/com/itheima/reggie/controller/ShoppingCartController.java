package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Chang
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("添加购物车");

        //设定用户id指定是哪位用户再购物
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者是套餐是否已经再购物车中(shoppingcart中可以有多条数据的，一个菜品或者一个套餐一条数据
        // 如果相同菜品连续添加就只需要再number上面加1就好)
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
            //获取dishid，如果dishid为空就说明这次添加的是套餐
        Long dishId = shoppingCart.getDishId();

        if(dishId != null){
            //获取的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else{
            //获取的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //到了此处querywrapper里面经过双层查询应该是只有一个结果了，把这个结果取出来
        //select* from shoppingcart where userId = ? and dishID = ?or setmealId = ?
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);

        //如果已经存在，就在原来基础上加1即可
        if(shoppingCartServiceOne != null ){

            shoppingCartServiceOne.setNumber(shoppingCartServiceOne.getNumber()+1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            //千万不要忘了最后一步更新回写
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            //如果不存在则需要新建一条，默认数量为一，新建一条的数据再传回来的shoppongcart里有
            //只要用shippingcartservice.save方法保存一下就好（回写）
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //为了统一返回对象
            shoppingCartServiceOne = shoppingCart;

        }




        return R.success(shoppingCartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);

        return R.success("删除成功");
    }


}
