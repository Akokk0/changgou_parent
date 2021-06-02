package com.changgou.file.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        try {
            // 判断文件是否存在
            if (file == null) {
                throw new RuntimeException("文件不存在！");
            }

            // 判断文件名是否为空
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isEmpty(originalFilename)) {
                throw new RuntimeException("文件不存在！");
            }

            // 获取文件拓展名
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            // 获取文件内容
            byte[] content = file.getBytes();

            // 创建文件实体类
            FastDFSFile fastDFSFile = new FastDFSFile(originalFilename, content, extName);

            // 上传文件
            String[] info = FastDFSClient.upload(fastDFSFile);

            // 封装返回结果
            String fileUrl = FastDFSClient.getTrackerUrl() + info[0] + "/" + info[1];
            return new Result(true, StatusCode.OK,"文件上传成功", fileUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "文件上传失败");
        }
    }
}
