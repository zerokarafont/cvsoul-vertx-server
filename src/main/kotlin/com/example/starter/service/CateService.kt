package com.example.starter.service

import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

data class CateModel(val _id: String, val name: String)

class CateService(private val client: MongoClient): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) { message ->
      val action = message.body().getString("ACTION")
      val data = message.body().getJsonObject("DATA")

      when(action) {
        "ALL" -> launch { message.reply(all()) }
      }
    }
  }

  private suspend fun all(): Any {
    val resp = client.find("cate", jsonObjectOf()).await()
    return jsonObjectOf(
      "statusCode" to 200,
      "msg" to "ok",
      "data" to resp
    )
  }
}
