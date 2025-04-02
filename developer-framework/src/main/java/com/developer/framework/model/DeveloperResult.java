package com.developer.framework.model;

import lombok.Data;

import java.util.List;

@Data
public class DeveloperResult<T> {

    private int code;

    private T data;

    private String msg;

    private long count;

    private Boolean isSuccessful;

    private String serialNo;

    /**返回成功 -- 集合 */
    public static <T> DeveloperResult<List<T>> success(String serialNo,List<T> data, long count){
        DeveloperResult<List<T>> result = new DeveloperResult<>();
        result.setCode(200);//成功
        result.setMsg("success");//提示语
        result.setData(data);
        result.setCount(count);
        result.setIsSuccessful(true);
        result.setSerialNo(serialNo);
        return result;
    }

    /**返回成功 -- 单个对象 */
    public static <T> DeveloperResult<T> success(String serialNo,T object){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(200);//成功
        result.setMsg("success");//提示语
        result.setData(object);//返回内容
        result.setIsSuccessful(true);
        result.setSerialNo(serialNo);
        return result;
    }

    /**返回成功 -- 空对象 */
    public static <T> DeveloperResult<T> success(String serialNo){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(200);//成功
        result.setMsg("success");//提示语
        result.setIsSuccessful(true);
        result.setSerialNo(serialNo);
        return result;
    }

    /**返回失败 */
    public static <T> DeveloperResult<T> error(String serialNo,int code, String msg){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(code);//失败
        result.setMsg(msg);//提示语
        result.setIsSuccessful(false);
        result.setSerialNo(serialNo);
        return result;
    }

    /**返回失败 */
    public static <T> DeveloperResult<T> error(String serialNo,String msg){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(500);//失败
        result.setMsg(msg);//提示语
        result.setIsSuccessful(false);
        result.setSerialNo(serialNo);
        return result;
    }

    /**返回失败 */
    public static <T> DeveloperResult<T> error(String serialNo){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(500);//失败
        result.setMsg("fail");//提示语
        result.setIsSuccessful(false);
        result.setSerialNo(serialNo);
        return result;
    }
}
