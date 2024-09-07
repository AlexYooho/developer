package com.developer.oss.util;

import com.alibaba.fastjson.JSON;
import com.developer.framework.utils.DateTimeUtils;
import com.developer.oss.config.MinioConfig;
import com.developer.oss.enums.FileTypeEnum;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MinioUtils {

    @Resource
    private MinioConfig minioConfig;

    @Resource
    private MinioClient minioClient;

    /**
     * @Description 桶是否存在
     * @Date 2023/11/7 14:19
     * @Author liaopenghui
     * @return boolean
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName){
        BucketExistsArgs args = BucketExistsArgs.builder().bucket(bucketName).build();
        return minioClient.bucketExists(args);
    }

    /**
     * @Description 创建存储桶
     * @Date 2023/11/7 14:37
     * @Author liaopenghui
     * @return void
     */
    @SneakyThrows
    public void makeBucket(String bucketName) {
        if (!bucketExists(bucketName)) {
            MakeBucketArgs args = MakeBucketArgs.builder().bucket(bucketName).build();
            minioClient.makeBucket(args);
        }
    }

    /**
     * 设置桶权限为public
     * @param bucketName
     */
    public void setBucketPublic(String bucketName){
        try {
            // 设置公开
            String sb = "{\"Version\":\"2012-10-17\"," +
                    "\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":" +
                    "{\"AWS\":[\"*\"]},\"Action\":[\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"," +
                    "\"s3:GetBucketLocation\"],\"Resource\":[\"arn:aws:s3:::" + bucketName +
                    "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" +
                    bucketName +
                    "/*\"]}]}";
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(sb)
                            .build());
        }catch (Exception ignored){

        }
    }

    /**
     * @Description 获取全部bucket
     * @Date 2023/11/7 14:37
     * @Author liaopenghui
     * @return
     */
    public List<Bucket> findAllBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            return buckets;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @Description  查看文件对象
     * @Date 2023/11/7 14:38
     * @Author liaopenghui
     * @return
     */
    public List<Item> findListObjects(String bucketName) {
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).build());
        List<Item> items = new ArrayList<>();
        try {
            for (Result<Item> result : results) {
                items.add(result.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }

    /**
     * @Description 查询单个储桶中的所有对象
     * @Date 2023/11/7 14:38
     * @Author liaopenghui
     * @return
     */
    public List<Object> findAllObjectByBucketName(String bucketName) throws Exception {
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build());
        Iterator<Result<Item>> iterator = results.iterator();
        List<Object> items = new ArrayList<>();
        String format = "{'fileName':'%s','fileSize':'%s'}";
        while (iterator.hasNext()) {
            Item item = iterator.next().get();
            items.add(JSON.parse(String.format(format, item.objectName(),
                    formatFileSize(item.size()))));
        }
        log.info("查询单个储桶中的所有对象信息：{}", items);
        return items;
    }

    /**
     * @Description 删除存储bucket
     * @Date 2023/11/7 14:38
     * @Author liaopenghui
     * @return
     */
    public Boolean deleteBucket(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @Description 删除一个对象
     * @Date 2023/11/7 14:38
     * @Author liaopenghui
     * @return
     */
    @SneakyThrows
    public boolean deleteObject(String bucketName, String objectName) {
        if (bucketExists(bucketName)) {
            RemoveObjectArgs args = RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build();
            minioClient.removeObject(args);
            return true;
        }
        return false;
    }

    /**
     * @Description 批量删除文件对象
     * @Date 2023/11/7 14:38
     * @Author liaopenghui
     * @return
     */
    public Iterable<Result<DeleteError>> batchDeleteObjects(String bucketName, List<String> objects) {
        List<DeleteObject> dos = objects.stream().map(e -> new DeleteObject(e)).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).objects(dos).build());
        return results;
    }

    /**
     * @Description 上传文件
     * @Date 2023/11/7 14:39
     * @Author liaopenghui
     * @return
     */
    public List<String> batchUploadFile(String bucketName, MultipartFile[] multipartFile) {
        List<String> names = new ArrayList<>(multipartFile.length);
        for (MultipartFile file : multipartFile) {
            String fileName = file.getOriginalFilename();
            String[] split = fileName.split("\\.");
            if (split.length > 1) {
                fileName = split[0] + "_" + System.currentTimeMillis() + "." + split[1];
            } else {
                fileName = fileName + System.currentTimeMillis();
            }
            InputStream in = null;
            try {
                in = file.getInputStream();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(in, in.available(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            names.add(fileName);
        }
        return names;
    }

    /**
     * @Description 文件上传
     * @Date 2023/11/7 14:39
     * @Author liaopenghui
     * @return
     */
    public String uploadFile(String bucketName,String fileType,MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String fileName = System.currentTimeMillis()+"";

        if(originalFileName.lastIndexOf(".")>=0){
            fileName+=originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String objectName = DateTimeUtils.getFormatDate(new Date(),DateTimeUtils.PARTDATEFORMAT)+"/"+fileName;

        try {
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(fileType+"/"+objectName)
                    .stream(file.getInputStream(),file.getSize(),-1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return objectName;
    }

    /**
     * @Description 获取文件外链
     * @Date 2023/11/7 14:39
     * @Author liaopenghui
     * @return
     */
    public String findFileUrl(String bucketName, FileTypeEnum fileType, String fileName){
        return minioConfig.getEndpoint()+"/"+bucketName+"/"+fileType.desc()+"/"+fileName;
    }

    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + " B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + " KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + " MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + " GB";
        }
        return fileSizeString;
    }

}
