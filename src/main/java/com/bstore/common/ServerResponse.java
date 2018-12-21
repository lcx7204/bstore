package com.bstore.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * create by Jakarta
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//保证序列化JSON的时候，如果值为null,则key值会消失
public class ServerResponse<T> implements Serializable {
    private int status;   //状态
    private String msg;    //消息
    private T data;        //数据

    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }

    /**
     * 操作失败时，返回状态，错误消息
     * @param status
     * @param msg
     */
    private ServerResponse(int status,String msg){
        this.status = status;
        this.msg = msg;
    }

    /**
     * 操作成功时，返回状态，消息，数据
     * @param status
     * @param msg
     * @param data
     */
    private ServerResponse(int status,String msg,T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    //返回状态（是否成功）
    @JsonIgnore
    //使之不在JSON序列化的结果当中
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus(){
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccess(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data){
    	return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }
    
    public static <T> ServerResponse<T> createBySuccess(String msg,T data){
    	return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new ServerResponse<T>(errorCode,errorMessage);
    }
}
