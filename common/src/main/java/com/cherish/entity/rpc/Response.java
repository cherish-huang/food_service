package com.cherish.entity.rpc;

import com.cherish.error.BaseErr;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@Builder
public class Response<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public boolean isSuccess(){
        return this.code == 0;
    }

    public static<T> Response<T> success(T data){
        return Response.<T>builder()
                          .code(0)
                          .message("success")
                          .data(data)
                          .build();
    }

    public static<T> Response<T> error(int code, String message){
        return Response.<T>builder()
                          .code(code)
                          .message(message)
                          .build();
    }

    public static<T> Response<T> error(BaseErr baseErr){
        return Response.<T>builder()
                .code(baseErr.getCode())
                .message(baseErr.getMessage())
                .build();
    }
}
