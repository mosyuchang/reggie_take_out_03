package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common" )
/**
 *
 */
public class CommonController {

    @Value("${reggie.path}")
    private String basepath;

    /**
     * 文件上传与下载
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){


        //当前的file 是一个临时文件，需要转存到指定的目录，否则本次请求完成后将会被删除
        log.info(file.toString());
        //原始文件的名称
        String originalFilename = file.getOriginalFilename();
        //裁剪原文件名的后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //使用uuid重新生成文件名称，防止重名覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建一个目录
        File dir = new File(basepath);
        //判断这个目录是否存在
        if(!dir.exists())
            //目录不存在需要创建
        {
            dir.mkdirs();
        }


        try {
            //将临时文件转存到指定位置
            file.transferTo(new File(basepath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //返回上传的文件名称
        return R.success(fileName);

    }

    /**
     * 下载方法
     * @param name
     * @param response
     */

    @GetMapping("/download")
    public void download(String name , HttpServletResponse response){

        try {
            //通过输入流读取文件
            FileInputStream fileInputStream = new FileInputStream(new File(basepath + name));
            //通过输出流把文件写回浏览器，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }


            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
