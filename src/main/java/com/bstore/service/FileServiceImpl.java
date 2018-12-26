package com.bstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * create by Jakarta
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    /**
     * 文件上传
     * @param file
     * @param path
     * @return
     */
    @Override
    public String uploadFile(MultipartFile file, String path) {
        //获取文件的名字
        String fileName = file.getOriginalFilename();
        //扩展名.jpg
//        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = System.currentTimeMillis() + "_" + fileName;
        logger.info("开始上传图片，上传的文件名:{},上传的路径:{},上传后的文件名:{}",fileName,path,uploadFileName);
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);  //设置为可写
            fileDir.mkdirs();   //创建文件夹及其子文件夹
        }
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);    //文件上传成功
            logger.info("文件上传成功");
        }catch (IOException e){
            logger.error("文件上传失败",e);
            return null;
        }
        return targetFile.getName();    //返回文件的名字
    }
}
