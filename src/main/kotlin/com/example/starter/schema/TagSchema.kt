package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 标签集合
 * @param _id
 * @param name 名称
 */
@Serializable
data class TagSchema(val _id: String, val name: String)
