package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;
import org.springframework.core.annotation.Order;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Chang
 * @Description:
 */
public interface OrderService extends IService<Orders> {

    public void reserveOrder(Orders orders);

    void submit(Orders orders);
}
