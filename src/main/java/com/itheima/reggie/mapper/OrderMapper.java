package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.core.annotation.Order;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Chang
 * @Description:
 */

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
