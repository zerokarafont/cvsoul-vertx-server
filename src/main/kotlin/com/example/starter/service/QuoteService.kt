package com.example.starter.service

import com.example.starter.constant.AuthAPI
import com.example.starter.constant.QuoteAPI
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.launch

class QuoteService(private val client: MongoClient): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name).handler { message ->
      val action = message.body().getString("ACTION")
      val params = message.body().getJsonObject("PARAMS")
      val sessionId = message.body().getString("SESSION_ID")
      val appKey = message.body().getString("APP_KEY")

      when(action) {
        QuoteAPI.FETCH_QUOTE_ALBUM_PAGINATION_LIST.name -> launch { message.reply(fetchQuoteAlbumPaginationList(params, sessionId, appKey)) }
      }
    }
  }

  fun fetchQuoteAlbumPaginationList(params: JsonObject, sessionId: String, appKey: String) {
    val page = params.getInteger("page")
    val pageSize = params.getInteger("pageSize")
    val title: String? = params.getString("title")
    val cateId: String? = params.getString("cateId")
  }
}
