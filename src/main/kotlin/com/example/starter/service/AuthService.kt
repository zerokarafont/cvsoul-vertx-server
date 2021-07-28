package com.example.starter.service

import com.example.starter.util.JWT
import com.example.starter.util.decryptData
import com.example.starter.util.decryptKeyDirectOrFromCache
import com.example.starter.util.encryptData
import com.soywiz.krypto.MD5
import io.vertx.core.CompositeFuture
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.launch

class AuthService(private val client: MongoClient) : CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name).handler { message ->
      val action = message.body().getString("ACTION")
      val data = message.body().getJsonObject("DATA")
      val sessionId = message.body().getString("SESSION_ID")
      val appKey = message.body().getString("APP_KEY")

      when(action) {
        "LOGIN" -> launch { message.reply(login(data, sessionId, appKey)) }
        "REGISTER" -> launch { message.reply(register(data, sessionId, appKey)) }
      }
    }
  }

  private suspend fun register(data: JsonObject, sessionId: String, appKey: String): Any {
    val username = data.getString("username")
    val encryptPass = data.getString("password")
    val encryptConfirmPass = data.getString("confirmPass")
    val code = data.getString("code")

    if (encryptPass != encryptConfirmPass) {
      return jsonObjectOf(
        "statusCode" to 400,
        "msg" to "输入密码不一致"
      )
    }

    val resp = CompositeFuture.all(
      client.findOne("user", jsonObjectOf(
        "username" to username
      ), jsonObjectOf(
        "username" to 1
      )),
      client.findOne("code", jsonObjectOf(
        "code" to code,
        "isUsed" to false
      ), jsonObjectOf()))
      .await()
      .result()
      .list<JsonObject?>()

    val userObj = resp[0]
    if (userObj != null) {
      return jsonObjectOf(
        "statusCode" to 400,
        "msg" to "用户名已存在"
      )
    }

    val codeObj = resp[1]
    if (codeObj == null) {
      return jsonObjectOf(
        "statusCode" to 400,
        "msg" to "无效邀请码"
      )
    }

    // 标志邀请码已使用
    client.updateCollection("code", jsonObjectOf(
      "code" to code
    ), jsonObjectOf(
      "\$set" to jsonObjectOf("isUsed" to true)
    )).await()

    // 解出明文密码
    val key = decryptKeyDirectOrFromCache(vertx, sessionId, appKey, config)
    val rawPass = decryptData<String>(encryptPass, key)

    val salt = config.getString("SALT")
    val hashPass = MD5.digest("$rawPass$salt".toByteArray()).hexUpper

    client.save("user", jsonObjectOf(
      "username" to username,
      "password" to hashPass
    )).await()

    return jsonObjectOf(
      "statusCode" to 200,
      "msg" to "注册成功",
      "data" to null
    )
  }

  private suspend fun login(data: JsonObject, sessionId: String, appKey: String): Any {
    val username = data.getString("username")
    val encryptPass = data.getString("password")

    val user = client.findOne("user", jsonObjectOf(
      "username" to username
    ), jsonObjectOf(
      "username" to 1
    )).await()

    if (user == null) {
      return jsonObjectOf(
        "statusCode" to 400,
        "msg" to "用户名不存在"
      )
    }

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

    // 生成jwt token
    val jwt = JWT.create(vertx).getAuth()
    val token = jwt!!.generateToken(jsonObjectOf("sub" to username))
    val encryptToken = encryptData(token, key)

    return jsonObjectOf(
      "statusCode" to 200,
      "msg" to "登录成功",
      "data" to encryptToken
    )
  }
}
