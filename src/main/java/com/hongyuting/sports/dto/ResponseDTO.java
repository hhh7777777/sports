package com.hongyuting.sports.dto;

import lombok.Data;

@Data
public class ResponseDTO {
    private int code;
    private String message;
    private Object data;

    public ResponseDTO(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResponseDTO success(String message) {
        return new ResponseDTO(200, message, null);
    }

    public static ResponseDTO success(String message, Object data) {
        return new ResponseDTO(200, message, data);
    }

    public static ResponseDTO error(String message) {
        return new ResponseDTO(500, message, null);
    }
}