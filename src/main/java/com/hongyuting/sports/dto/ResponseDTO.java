package com.hongyuting.sports.dto;

import lombok.Data;

import java.util.Collection;

/**
 * 响应数据传输对象
 */
@Data
public class ResponseDTO {
    private int code;
    private String message;
    private Object data;
    /**
 * 构造函数，用于创建响应对象
 */
    public ResponseDTO(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    /**
 * 静态方法，用于创建成功响应
 */
    public static ResponseDTO success(String message) {
        return new ResponseDTO(200, message, null);
    }
    /**
 * 静态方法，用于创建带数据的成功响应
 */
    public static ResponseDTO success(String message, Object data) {
        return new ResponseDTO(200, message, data);
    }
 /**
 * 静态方法，用于创建错误响应
 */
    public static ResponseDTO error(String message) {
        return new ResponseDTO(500, message, null);
    }
 /**
 * 静态方法，用于创建带错误码的错误响应
 */
    public static ResponseDTO error(int code, String message) {
        return new ResponseDTO(code, message, null);
    }
    /**
 * 静态方法，用于创建带错误码和数据的错误响应
 */
    public static ResponseDTO error(int code, String message, Object data) {
        return new ResponseDTO(code, message, data);
    }
    /**
 * 判断响应是否成功
 */
    public boolean isSuccess() {
        return code == 200;
    }
}