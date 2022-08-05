package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Chang
 * @Description:
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveOrder(Orders orders) {
        //盘清楚逻辑，第一不知道用户是谁所以获得用户id
        Long userId = BaseContext.getCurrentId();
        //查询当前用户的购物车数据,注意，一个userId是有多条shoppingcart数据的
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        //判断一下是否为空
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据,先要从order里面拿到addressbookid（主键）
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);

        //判断一下地址簿是否为空
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //向订单表插入数据，一条数据（insert）
            //创建订单号
        Long orderId = IdWorker.getId();
        orders.setNumber(String.valueOf(orderId));
            //插入数值
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());

        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setStatus(2);
        orders.setUserName(user.getName());
        orders.setUserId(user.getId());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setAddress((addressBook.getProvinceName() == null ?" ": addressBook.getProvinceName())
                +(addressBook.getCityName() == null ?" ": addressBook.getCityName())
                +(addressBook.getDistrictName() == null ?" ": addressBook.getDistrictName())
                +(addressBook.getDetail() == null ?" ": addressBook.getDetail()));

        this.save(orders);


        //像订单明细表插入多条数据


        //清空购物车数据


    }
}
