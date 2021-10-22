package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 角色信息
 * @param name 名字
 * @param avatar 头像
 * @param cv 声优
 */
@Serializable
data class CharacterSchema(val name: String, val avatar: String, val cv: String)

/**
 * 音频集合
 * @param isOpen 是否开放
 * @param character 角色信息 CharacterSchema
 * @param url 资源链接
 * @param text 文本内容
 * @param pronounce 发音
 * @param translate 翻译
 * @see CharacterSchema
 */
@Serializable
data class VoiceSchema(
  val isOpen: Boolean,
  val character: CharacterSchema,
  val url: String,
  val text: String,
  val pronounce: String,
  val translate: String
):CommonSchema()
