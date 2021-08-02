package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 语录集合
 * @param userId 所属创建用户
 * @param isOpen 是否开放
 * @param cover 封面
 * @param title 标题
 * @param desc 描述
 * @param tags 标签 List<String?>
 */
@Serializable
data class QuoteAlbumSchema(
  val userId: String,
  val isOpen: Boolean,
  val cover: String,
  val title: String,
  val desc: String,
  val tags: List<String?>
  ): CommonSchema()
