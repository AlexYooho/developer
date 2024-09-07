package com.developer.oss.service.impl;

import com.developer.framework.constant.DeveloperConstant;
import com.developer.framework.context.SelfUserInfoContext;
import com.developer.framework.model.DeveloperResult;
import com.developer.framework.utils.FileUtil;
import com.developer.oss.dto.UploadImageDTO;
import com.developer.oss.enums.FileTypeEnum;
import com.developer.oss.service.FileService;
import com.developer.oss.service.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private MinioService minioService;


    /**
     * 上传文件
     * @param file
     * @return
     */
    @Override
    public DeveloperResult uploadFile(MultipartFile file) {
        Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
        if(file.getSize()> DeveloperConstant.MAX_FILE_SIZE){
            return DeveloperResult.error("文件大小不能超过10M");
        }

        String fileName = minioService.uploadFile("developer","file",file);
        if(StringUtils.isEmpty(fileName)){
            return DeveloperResult.error("文件上传失败");
        }
        String fileUrl = minioService.findFileUrl("developer", FileTypeEnum.FILE,fileName);
        return DeveloperResult.success(fileUrl);
    }

    /**
     * 上传图片
     * @param file
     * @return
     */
    @Override
    public DeveloperResult uploadImage(MultipartFile file) {
        try{
            Long userId = SelfUserInfoContext.selfUserInfo().getUserId();
            if(file.getSize()> DeveloperConstant.MAX_IMAGE_SIZE){
                return DeveloperResult.error("图片大小不能超过5M");
            }
            if(!FileUtil.isImage(file.getOriginalFilename())){
                return DeveloperResult.error("图片格式不合法");
            }

            UploadImageDTO uploadImageRep = new UploadImageDTO();
            String fileName = minioService.uploadFile("developer","image",file);
            if(StringUtils.isEmpty(fileName)){
                return DeveloperResult.error("图片上传失败");
            }

            String fileUrl = minioService.findFileUrl("developer",FileTypeEnum.IMAGE,fileName);
            uploadImageRep.setOriginUrl(fileUrl);
            // 大于30k上传缩略图
            if(file.getSize()>30*1024){
                byte[] imageByte={};
                fileName = minioService.uploadFile("developer","image",file);
                if(StringUtils.isEmpty(fileName)){
                    return DeveloperResult.error("图片上传失败");
                }
            }
            fileUrl = minioService.findFileUrl("developer",FileTypeEnum.IMAGE,fileName);
            uploadImageRep.setThumbUrl(fileUrl);
            return DeveloperResult.success(uploadImageRep);
        }catch (Exception e){
            log.error("上传图片失败,{}",e.getMessage(),e);
            return DeveloperResult.error(500,"图片上传失败");
        }
    }
}
