package model;

import lombok.Data;

import java.util.List;

@Data
public class DeveloperResult {

    private int code;

    private Object data;

    private String msg;

    private long count;

    /**返回成功 */
    public static DeveloperResult success(List<Object> data, long count){
        DeveloperResult result = new DeveloperResult();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        result.setData(data);
        result.setCount(count);
        return result;
    }

    /**返回成功 */
    public static DeveloperResult success(List data){
        DeveloperResult result = new DeveloperResult();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        result.setData(data);
        result.setCount(data == null || data.size() == 0 ? 0 : data.size());
        return result;
    }

    /**返回成功 */
    public static DeveloperResult successForPage(List data,Integer count){
        DeveloperResult result = new DeveloperResult();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        result.setData(data);
        result.setCount(count == null ? 0 : count);
        return result;
    }

    /**返回成功 */
    public static DeveloperResult success(){
        DeveloperResult result = new DeveloperResult();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        return result;
    }

    /**返回成功 */
    public static DeveloperResult success(Object object){
        DeveloperResult result = new DeveloperResult();
        result.setCode(200);//成功
        result.setMsg("成功");//提示语
        result.setData(object);//返回内容
        return result;
    }

    /**返回失败 */
    public static DeveloperResult error(){
        DeveloperResult result = new DeveloperResult();
        result.setCode(500);//失败
        result.setMsg("失败");//提示语
        return result;
    }

    /**返回失败 */
    public static DeveloperResult error(String msg){
        DeveloperResult result = new DeveloperResult();
        result.setCode(500);//失败
        result.setMsg(msg);//提示语
        return result;
    }

    /**返回失败 */
    public static DeveloperResult error(int code, String msg){
        DeveloperResult result = new DeveloperResult();
        result.setCode(code);//失败
        result.setMsg(msg);//提示语
        return result;
    }

    /**返回信息*/
    public static DeveloperResult response(int code, String msg, Object data) {
        DeveloperResult result = new DeveloperResult();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
