package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 声优信息
 * @param name 姓名
 */
@Serializable
data class CVSchema(val name: String)

/**
 * 音频集合
 * @param isOpen 是否开放
 * @param cv 声优信息 CVSchema
 * @param url 资源链接
 * @param text 文本内容
 * @param pronounce 发音
 * @param translate 翻译
 * @see CVSchema
 */
@Serializable
data class VoiceSchema(
  val isOpen: Boolean,
  val cv: CVSchema,
  val url: String,
  val text: String,
  val pronounce: String,
  val translate: String
):CommonSchema()
