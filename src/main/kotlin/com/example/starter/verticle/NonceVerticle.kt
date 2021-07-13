package com.example.starter.verticle

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle

/**
 * 缓存每次HTTP请求的nonce值
 */
class NonceVerticle: CoroutineVerticle() {
  // <nonce值， 存入时间>
  private val cache = HashMap<String, Long>()

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name) {
      val action = it.body().getString("ACTION")
      val nonce = it.body().getString("MESSAGE")
      when(action) {
        "GET" -> {
          if (cache[nonce] == null) {
            it.reply("")
          }else {
            it.reply(nonce)
          }
        }
        "SET" -> {
          cache[nonce] = System.currentTimeMillis()
        }
      }
    }
  }
}
