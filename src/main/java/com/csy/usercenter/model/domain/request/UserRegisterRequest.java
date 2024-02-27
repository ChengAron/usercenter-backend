package com.csy.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 *  用户注册请求体
 *
 * @author csy
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 4262463547537737L;

    private String userAccount;

    private String userPassword;

    private String checkPassword;

    private String planetCode;
}
