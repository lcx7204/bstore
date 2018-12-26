package com.bstore.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * create by Jakarta
 */
public interface IFileService {
    String uploadFile(MultipartFile file,String path);
}
