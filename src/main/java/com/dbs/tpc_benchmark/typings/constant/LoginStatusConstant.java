package com.dbs.tpc_benchmark.typings.constant;

/**
 * 状态常量
 * 0：登录成功
 * 1：登录失败，但还有机会尝试
 * 2：登录失败超过3次，账户冻结
 * 9：发生错误
 */
public class LoginStatusConstant {
    public static final Integer LOGIN_SUCCESS = 0;
    public static final Integer LOGIN_FAILED_CAN_TRY = 1;
    public static final Integer LOGIN_FAILED_AND_STOP= 2;
    public static final Integer REQUEST_ERROR = 9;
}
