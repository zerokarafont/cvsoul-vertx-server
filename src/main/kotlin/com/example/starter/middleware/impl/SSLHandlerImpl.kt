package com.example.starter.middleware.impl

import com.example.starter.middleware.SSLHandler
import com.example.starter.util.RSA
import com.example.starter.verticle.SSLVerticle
import com.soywiz.krypto.encoding.Base64
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * SSL会话协商及缓存逻辑
 */
class SSLHandlerImpl(private val vertx: Vertx, private val config: JsonObject): SSLHandler {
  override fun handle(ctx: RoutingContext) {
    val headers = ctx.request().headers()
    val sessionId = headers.get("sessionId")

    CoroutineScope(vertx.dispatcher()).launch {
      val GET_REQUEST = jsonObjectOf(
        "ACTION" to "GET",
        "SESSION_ID" to sessionId
      )
      val cacheKey = vertx.eventBus().request<JsonObject>(SSLVerticle::class.java.name, GET_REQUEST).await().body().getString("key")

      if (cacheKey.isNullOrEmpty()) {

        val appKey = headers.get("appKey")
        val privateKey = config.getString("PRIVATE_KEY")
        val rawKey = RSA.decryptMessage(Base64.decode(appKey), privateKey)

        /**
         * NOTICE: 刚好过期的请求携带的appkey是旧的 不能和新的sessionId一起缓存, 应该开始建立3次握手交换新的密钥
         * 1. 服务端发送响应头 header("sessionId", "expired") 告诉客户端此加密会话已经过期，需要生成新的密钥
         * 2. 客户端识别到expired信息, 生成新的密钥, 发送请求头 header("sessionId", "update") 告诉服务端密钥更新了
         * 3. 服务端识别到update信息拿到新的密钥同时生成新的sessionId, 发送响应头 header("sessionId", newSessionId), 并缓存到redis中
         */

        when {
          sessionId.isNullOrEmpty() -> {
            // 客户端第一次建立会话
            val newSessionId = cacheNewSessionKeyMap(rawKey)
            ctx.response().headers().set("sessionId", newSessionId)
          }
          sessionId == "update" -> {
            // 客户端更新密钥, 要求服务端返回新的sessionId, 并缓存新的密钥
            val newSessionId = cacheNewSessionKeyMap(rawKey)
            ctx.response().headers().set("sessionId", newSessionId)
          }
          else -> {
            // 客户端会话过期, 服务端发送 expired标识 要求客户端更新密钥
            ctx.response().headers().set("sessionId", "expired")
          }
        }
        ctx.next()
      } else {
        // 如果未过期返回原来的sessionId
        ctx.response().headers().set("sessionId", sessionId)
        ctx.next()
      }
    }
  }

  private fun cacheNewSessionKeyMap(rawKey: String): String {
    val newSessionId = UUID.randomUUID().toString()
    val SET_REQUEST = jsonObjectOf(
      "ACTION" to "SET",
      "SESSION_ID" to newSessionId,
      "KEY" to rawKey
    )

    // 缓存sessionId
    vertx.eventBus().send(SSLVerticle::class.java.name, SET_REQUEST)

    println("newSessionId: $newSessionId")

    return newSessionId
  }
}
