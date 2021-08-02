package com.example.starter.schema

import kotlinx.serialization.Serializable

/**
 * 语录-音频 多对多集合
 *@param voiceId 音频Id
 * @param quoteId 语录集Id
 */
@Serializable
data class QuoteTOVoiceSchema(
  val voiceId: String,
  val quoteId: String
): CommonSchema()
