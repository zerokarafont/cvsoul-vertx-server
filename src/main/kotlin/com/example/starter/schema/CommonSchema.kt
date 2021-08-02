package com.example.starter.schema

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
