package com.example.starter.verticle

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.redis.client.psetexAwait
import io.vertx.redis.client.RedisAPI
import kotlinx.coroutines.launch

/**
 * 缓存每次HTTP请求的nonce值
 */
class NonceVerticle(private val redis: RedisAPI): CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) {
      val action = it.body().getString("ACTION")
      val nonce = it.body().getString("MESSAGE")
      when(action) {
        "GET" -> {
          launch {
            val isExist = redis.exists(mutableListOf("nonce:$nonce")).await()
            it.reply(jsonObjectOf(
              "isExist" to isExist.toBoolean()
            ))
          }
        }
        "SET" -> {
          // 过期时间60秒
          redis.setex("nonce:$nonce", config.getNumber("NONCE_MIN_EXPIRED_UNIT_SEC").toString(), "1")
        }
      }
    }
  }
}
