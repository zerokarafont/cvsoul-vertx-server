package com.example.starter.verticle

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.RedisAPI
import kotlinx.coroutines.launch

/**
 * 第一次验证签名通过返回一个sessionId并缓存 sessionId -> aesKey
 * 缓存有效期为一天, 在此期间不再进行RSA解密
 */
class SSLVerticle(private val redis: RedisAPI): CoroutineVerticle() {
  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) {
      val action = it.body().getString("ACTION")
      val sessionId = it.body().getString("SESSION_ID")
      val key = it.body().getString("KEY")
      when(action) {
        "GET" -> {
          launch {
            val cacheKey = redis.get("sessionId:$sessionId").await() ?: ""

            it.reply(jsonObjectOf(
              "key" to cacheKey.toString()
            ))
          }
        }
        "SET" -> {
          // 过期时间一天
          redis.setex("sessionId:$sessionId", config.getNumber("SSL_DAY_EXPIRED_UNIT_SEC").toString(), key)
        }
      }
    }
  }
}
