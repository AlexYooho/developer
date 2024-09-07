package com.developer.framework.utils;

public class FileUtil {

    /**
     * 获取文件扩展名
     * @param fileName
     * @return
     */
    public static String getFileExtension(String fileName){
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

    /**
     * 判断是否是图片格式
     * @param fileName
     * @return
     */
    public static boolean isImage(String fileName){
        String fileExtension = getFileExtension(fileName);
        String[] imageExtensionArr = {"jpeg", "jpg", "bmp", "png","webp","gif"};
        for (String item:imageExtensionArr){
            if(fileExtension.toLowerCase().equals(item)){
                return true;
            }
        }
        return false;
    }
}
