package com.example.starter.service

import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

class UserService(private val client: MongoClient): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) { message ->
      val action = message.body().getString("ACTION")
      val data = message.body().getJsonObject("DATA")

      when(action) {
        "PROFILE" -> launch { message.reply(profile(data)) }
      }
    }
  }

  private suspend fun profile(user: JsonObject): Any {
    val username = user.getString("sub")

    val userInfo = client.findOne("user", jsonObjectOf(
      "username" to username
    ), jsonObjectOf(
      "username" to 1,
      "avatar" to 1
    )).await()

    if (userInfo == null) {
      return jsonObjectOf(
        "statusCode" to 400,
        "msg" to "未查询到用户信息",
        "data" to null
      )
    }

    return jsonObjectOf(
      "statusCode" to 200,
      "msg" to "ok",
      "data" to userInfo
    )
  }
}
