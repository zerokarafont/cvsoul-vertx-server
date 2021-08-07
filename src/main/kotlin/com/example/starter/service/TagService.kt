package com.example.starter.service

import com.example.starter.constant.CollectionSchema
import com.example.starter.constant.TagAPI
import com.example.starter.util.responseOk
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

class TagService(private val client: MongoClient): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) { message ->
      val action = message.body().getString("ACTION")
      val params = message.body().getJsonObject("PARAMS")

      when(action) {
        TagAPI.ALL.name -> launch { message.reply(all()) }
      }
    }
  }

  private suspend fun all(): Any {
    val resp = client.find(CollectionSchema.TAG.name.lowercase(), jsonObjectOf()).await()
    return responseOk(data = resp)
  }
}