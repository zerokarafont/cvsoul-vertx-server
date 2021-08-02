package com.example.starter.constant

import com.example.starter.schema.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.StructureKind

enum class CollectionSchema(value: String) {
  /**
   * 用户集合
   */
  USER("user"),

  /**
   * 邀请码集合
   */
  CODE("code"),

  /**
   * 分类集合
   */
  CATE("cate"),

  /**
   * 标签集合
   */
  TAGS("tags"),

  /**
   * 音频集合
   */
  VOICE("voice"),

  /**
   * 语录集合
   */
  QUOTE_ALBUM("quote_album"),

  /**
   * 语录音频-多对多关系集合
   */
  QUOTE_ALBUM_N_TO_N_VOICE("quote_album_N_to_N_voice"),
}
