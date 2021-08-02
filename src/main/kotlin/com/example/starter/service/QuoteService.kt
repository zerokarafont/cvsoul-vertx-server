package com.example.starter.service

import com.example.starter.constant.CollectionSchema
import com.example.starter.constant.QuoteAPI
import com.example.starter.schema.QuoteAlbumSchema
import com.example.starter.util.decryptKeyDirectOrFromCache
import com.example.starter.util.encryptData
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

class QuoteService(private val client: MongoClient): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name).handler { message ->
      val action = message.body().getString("ACTION")
      val params = message.body().getJsonObject("PARAMS")
      val body = message.body().getJsonObject("BODY")
      val sessionId = message.body().getString("SESSION_ID")
      val appKey = message.body().getString("APP_KEY")
      val isAdmin = message.body().getBoolean("IS_ADMIN")

      when(action) {
        QuoteAPI.FETCH_QUOTE_ALBUM_PAGINATION_LIST.name -> launch { message.reply(fetchQuoteAlbumPaginationList(params, sessionId, appKey, isAdmin)) }
      }
    }
  }

  private suspend fun fetchQuoteAlbumPaginationList(params: JsonObject, sessionId: String, appKey: String, isAdmin: Boolean? = false): Any {
    val page = params.getInteger("page")
    val pageSize = params.getInteger("pageSize")
    val title: String? = params.getString("title")
    val cateId: String? = params.getString("cateId")

    val fields = jsonObjectOf()
    val sort = jsonObjectOf()
    val query = jsonObjectOf()

    if (title != null && title.isNotEmpty()) {
      query.put("title",Regex.escape("/$title/"))
    }
    if (cateId != null && cateId.isNotEmpty()) {
      query.put("cateId", cateId)
    }

    if (isAdmin != true) {
      // 如果是APP端请求
      query.put("isOpen", true)
      fields.put("cover", 1).put("title", 1)
    }else {
      // 如果是ADMIN端请求
      // 按照更新时间降序返回
      sort.put("updateTime", -1)
    }

    val options = FindOptions(jsonObjectOf(
      "fields" to fields,
      "sort" to sort,
      "limit" to pageSize,
      "skip" to page - 1
    ))

    val resp = client.findWithOptions(CollectionSchema.QUOTE_ALBUM.name, query, options).await()
    val key = decryptKeyDirectOrFromCache(vertx, sessionId, appKey, config)
    val data = encryptData(Json.encodeToString(resp), key)

    return jsonObjectOf(
      "statusCode" to 200,
      "msg" to "ok",
      "data" to data
    )

  }
}
