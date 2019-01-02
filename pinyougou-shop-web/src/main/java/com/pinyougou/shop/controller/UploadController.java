package com.pinyougou.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        String originalFilename = file.getOriginalFilename();// 获取绝对
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);

        try {
            FastDFSClient client =  new FastDFSClient("classpath:config/fdfs_client.conf");
            String fileId = client.uploadFile(file.getBytes(), extName);
            String url = FILE_SERVER_URL + fileId;
            return  new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
    @RequestMapping("/upload2")
    public Map upload2(MultipartFile imgFile){

        String originalFilename = imgFile.getOriginalFilename();// 获取绝对
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);

        try {
            FastDFSClient client =  new FastDFSClient("classpath:config/fdfs_client.conf");
            String fileId = client.uploadFile(imgFile.getBytes(), extName);
            String url = FILE_SERVER_URL + fileId;
            Map map= new HashMap();
            map.put("error",0);
            map.put("url",url);
            return  map;
        } catch (Exception e) {
            e.printStackTrace();
            Map map= new HashMap();
            map.put("error",1);
            map.put("url","上传失败");
            return  map;
        }
    }
}
