package com.fengrui.shortlink.common.convention.errorcode;
public enum BaseErrorCode implements IErrorCode{

    SUCCESS("0", "成功"),

    // ========== 一级宏观错误码 客户端错误 ==========
    CLIENT_ERROR("A000001", "用户端错误"),
    USER_REGISTER_ERROR("A000100", "用户注册错误"),
    USER_NAME_VERIFY_ERROR("A000110", "用户名校验失败"),
    USER_NAME_EXIST_ERROR("A000111", "用户名已存在"),
    USER_NAME_SENSITIVE_ERROR("A000112", "用户名包含敏感词"),
    USER_NAME_SPECIAL_CHARACTER_ERROR("A000113", "用户名包含特殊字符"),
    PASSWORD_VERIFY_ERROR("A000120", "密码校验失败"),
    PASSWORD_SHORT_ERROR("A000121", "密码长度不够"),
    PHONE_VERIFY_ERROR("A000151", "手机格式校验失败"),

    // ========== 二级宏观错误码 用户注册错误 ==========

    USER_TOKEN_FAIL("A00200", "用户Token验证失败"),

    USER_PASSWORD_WRONG("A00201", "用户密码错误"),

    USER_LOGIN_ERROR("A00202", "用户登陆错误"),

    USER_NULL("B000200", "用户记录不存在"),

    USER_NAME_EXIST("B000201", "用户名已存在"),

    USER_EXIST("B000202", "用户记录已存在"),

    USER_SAVE_ERROR("B000203", "用户记录新增失败"),

    // ========== 二级宏观错误码 短链接管理 ==========

    LINK_GENERATE_TOO_FREQUENT("B000301", "短链接频繁生成，请稍后再试"),

    LINK_EXISTS("B000302", "短链接已存在"),

    LINK_NOT_EXISTS("B000303", "短链接不存在"),

    LINK_BATCH_GENERATE_ERROR("B000304", "批量生产失败"),

    LINK_UPDATE_LOCK("B000304", "短链接正在被访问，请稍后再试"),

    // ========== 二级宏观错误码 短链接统计异常 ==========
    LINK_STATS_ERROR("B000400", "短链接访问量统计异常"),


    // ========== 二级宏观错误码 系统请求缺少幂等Token ==========
    IDEMPOTENT_TOKEN_NULL_ERROR("A000200", "幂等Token为空"),
    IDEMPOTENT_TOKEN_DELETE_ERROR("A000201", "幂等Token已被使用或失效"),

    // ========== 二级宏观错误码 系统请求操作频繁 ==========
    FLOW_LIMIT_ERROR("A000300", "当前系统繁忙，请稍后再试"),

    // ========== 一级宏观错误码 系统执行出错 ==========
    SERVICE_ERROR("B000001", "系统执行出错"),
    // ========== 二级宏观错误码 系统执行超时 ==========
    SERVICE_TIMEOUT_ERROR("B000100", "系统执行超时"),
    SERVICE_BLOOM_FILTER_IMPORT_ERROR("B000101", "布隆过滤器初始化失败"),

    // ========== 二级宏观错误码 分组错误 ==========
    SERVICE_MAX_GROUP_NUM("B000400", "超出最大分组数"),

    SERVICE_NO_GROUP("B000401", "用户无分组信息"),

    // ========== 一级宏观错误码 调用第三方服务出错 ==========
    REMOTE_ERROR("C000001", "调用第三方服务出错");

    private final String code;

    private final String message;

    BaseErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
