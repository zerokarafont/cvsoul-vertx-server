package com.example.starter.service

import com.example.starter.util.decryptData
import com.example.starter.util.decryptKeyDirectOrFromCache
import com.example.starter.util.isRequestSessionIdValid
import com.soywiz.krypto.AES
import com.soywiz.krypto.MD5
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.Base64
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

class AuthService(private var client: MongoClient) : CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name).handler { message ->
      val action = message.body().getString("ACTION")
      val data = message.body().getJsonObject("DATA")
      val sessionId = message.body().getString("SESSION_ID")
      val appKey = message.body().getString("APP_KEY")

      when(action) {
        "LOGIN" -> launch { message.reply(login(data, sessionId, appKey)) }
      }
    }
  }

  private suspend fun login(data: JsonObject, sessionId: String, appKey: String): Any {
    val username = data.getString("username")
    val encryptPass = data.getString("password")

    // 解出明文密码
    val key = decryptKeyDirectOrFromCache(vertx, sessionId, appKey, config)
    val rawPass = decryptData<String>(encryptPass, key)

    // hash后和数据库对比
    val salt = config.getString("SALT")
    val hashPass = MD5.digest("$rawPass$salt".toByteArray()).hexUpper
    val passInDB = client.findOne("user", jsonObjectOf(
      "username" to username
    ), jsonObjectOf(
      "password" to 1
    )).await()?.getString("password")

    if (hashPass != passInDB) {
      return jsonObjectOf(
        "statusCode" to 400,
        "msg" to "密码错误",
        "data" to null
      )
    }

    return jsonObjectOf(
      "statusCode" to 200,
      "msg" to "登录成功",
      "data" to "token"
    )
  }
}
