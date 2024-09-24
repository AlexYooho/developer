package com.developer.framework.model;

import lombok.Data;

import java.util.List;

@Data
public class DeveloperResult<T> {

    private int code;

    private T data;

    private String msg;

    private long count;

    /**返回成功 -- 集合 */
    public static <T> DeveloperResult<List<T>> success(List<T> data, long count){
        DeveloperResult<List<T>> result = new DeveloperResult<>();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        result.setData(data);
        result.setCount(count);
        return result;
    }

    /**返回成功 -- 单个对象 */
    public static <T> DeveloperResult<T> success(T object){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        result.setData(object);//返回内容
        return result;
    }

    /**返回成功 -- 空对象 */
    public static <T> DeveloperResult<T> success(){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        return result;
    }

    /**返回失败 */
    public static <T> DeveloperResult<T> error(int code, String msg){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(code);//失败
        result.setMsg(msg);//提示语
        return result;
    }

    /**返回失败 */
    public static <T> DeveloperResult<T> error(String msg){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(500);//失败
        result.setMsg(msg);//提示语
        return result;
    }

    /**返回失败 */
    public static <T> DeveloperResult<T> error(){
        DeveloperResult<T> result = new DeveloperResult<>();
        result.setCode(500);//失败
        result.setMsg("失败");//提示语
        return result;
    }
}
