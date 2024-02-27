package com.csy.usercenter.common;

/**
 *  返回工具类
 *
 * @author csy
 */
public class ResultUtils {
    /**
     *  成功
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     *  失败
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse error(ErrorCode errorCode, String message, String s) {
        return new BaseResponse(errorCode.getCode(),null,message,s);
    }

    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code,null,message,description);
    }
}
