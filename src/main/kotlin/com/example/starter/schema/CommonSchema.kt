package com.example.starter.schema

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
