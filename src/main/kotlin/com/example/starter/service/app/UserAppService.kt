package com.example.starter.service.app

import com.example.starter.constant.UserAPI
import com.example.starter.util.responseException
import com.example.starter.util.responseOk
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

class UserAppService(private val client: MongoClient): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) { message ->
      val action = message.body().getString("ACTION")
      val params = message.body().getJsonObject("PARAMS")

      when(action) {
        UserAPI.Profile.name -> launch { message.reply(profile(params)) }
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
      return responseException(msg = "未查询到用户信息")
    }

    return responseOk(data = userInfo)
  }
}
