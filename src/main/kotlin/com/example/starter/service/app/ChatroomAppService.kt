package com.example.starter.service.app

import com.example.starter.constant.ChatroomAPI
import com.example.starter.constant.CollectionSchema
import com.example.starter.service.common.BaseService
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import kotlinx.coroutines.launch

class ChatroomAppService(private val client: MongoClient): BaseService(client, CollectionSchema.CHATROOM.name) {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name).handler { message ->
      val action = message.body().getString("ACTION")
      val params = message.body().getJsonObject("PARAMS")
      val body = message.body().getJsonObject("BODY")

      when(action) {
        ChatroomAPI.FETCH_CHATROOM_PAGINATION_LIST.name -> launch { message.reply(fetchChatroomPaginationList(params)) }
      }
    }
  }

  private suspend fun fetchChatroomPaginationList(params: JsonObject): JsonObject {
    val query  = jsonObjectOf("isOpen" to true)
    val fields = jsonObjectOf("cover" to 1, "title" to 1)
    val sort   = jsonObjectOf()
    val title: String?  = params.getString("title")
    if (title?.isNotEmpty() == true) {
      query.put("title", jsonObjectOf("\$regex" to title))
    }
    return super.fetchPaginationList(params = params, query = query, fields = fields, sort = sort)
  }
}
