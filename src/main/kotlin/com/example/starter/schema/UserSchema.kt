package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 用户集合
 * @param _id
 * @param username 用户名
 * @param password 密码
 * @param avatar 头像
 */
@Serializable
data class UserSchema(val _id: String, val username: String, val password: String, val avatar: String)
