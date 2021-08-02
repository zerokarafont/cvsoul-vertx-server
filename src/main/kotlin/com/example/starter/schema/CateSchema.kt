package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 分类集合
 * @param _id
 * @param name 名称
 */
@Serializable
data class CateSchema(val _id: String, val name: String)
