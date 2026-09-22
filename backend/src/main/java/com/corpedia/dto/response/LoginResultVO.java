package com.corpedia.dto.response;

/**
 * 登录返回体：JWT 令牌 + 用户信息。
 */
public record LoginResultVO(
        /** 签发的 JWT，前端存本地并随 Authorization: Bearer 携带。 */
        String token,
        /** 当前登录用户信息。 */
        UserInfoVO user
) {
}