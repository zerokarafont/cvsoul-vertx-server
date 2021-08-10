package com.example.starter.schema

import io.vertx.core.json.JsonObject
import kotlinx.serialization.Serializable

/**
 * @param _id ObjectID
 * @param createTime 创建时间 timestamp
 * @param updateTime 更新时间 timestamp
 */
open class CommonSchema {
  lateinit var _id: String
  lateinit var createTime: String
  lateinit var updateTime: String
}

/**
 * 分页响应格式
 * @param page 当前页码
 * @param pageSize 每页大小
 * @param data List<T>
 */
@Serializable
data class PaginationSchema<T>(val page: Int, val pageSize: Int, val data: T)

/**
 * 返回响应格式
 * @param statusCode 状态码
 * @param msg 信息
 * @param data 数据
 */
@Serializable
data class ResponseSchema<T>(val statusCode: String, val msg: String, val data: T? = null)

/**
 * 请求内容格式
 * @param params GET请求内容
 * @param body POST请求内容
 */
data class RequestSchema(val params: JsonObject, val body: JsonObject)
