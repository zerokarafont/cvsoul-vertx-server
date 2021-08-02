package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 邀请码集合
 * @param code 邀请码
 * @param isUsed 是否使用
 */
@Serializable
data class CodeSchema(val code: String, val isUsed: Boolean): CommonSchema()
