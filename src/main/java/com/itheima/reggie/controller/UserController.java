package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: Chang
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
     private UserService userService;

     @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        //判断一下手机号是否为空
        if(!phone.isEmpty()){
            //生成4为验证码,转为string好比对
            String code = ValidateCodeUtils.generateValidateCode4String(4).toString();
            //没有云服务，打印一下code
            log.info("code是{}",code);
            //调用阿里云提供的短信服务
            //SMSUtils.sendMessage("外卖","",phone,code);
            //将需要生成验证码存到session中
            session.setAttribute(phone,code);
            return R.success("手机验证码短信发送成功");
        }


        return R.error("发送失败");
    }

    /**
     * 用户移动端登入
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取用户从手机上得到的验证码
        String code = map.get("code").toString();

        //获取在session中保存的验证码
        Object codeInSeesion = session.getAttribute(phone);
        //进行验证码比对
        if(codeInSeesion != null && codeInSeesion.equals(code)) {
            //如果比对成功说明可以登入
            //判断当前用户是否是新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if(user == null){
                user =  new User();
                user.setPhone(phone);
                //不要忘了写入数据库
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }



        return R.error("登入失败");
    }

    /**
     * 退出方法
     * @return
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request){

        request.getSession().removeAttribute("user");

        return R.success("退出成功");
    }

}
