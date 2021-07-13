package com.example.starter.middleware.impl

import com.example.starter.middleware.SignHandler
import com.example.starter.util.RSA
import com.example.starter.verticle.NonceVerticle
import com.soywiz.krypto.MD5
import com.soywiz.krypto.encoding.Base64
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.impl.headers.HeadersMultiMap
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SignHandlerImpl(private val vertx: Vertx, private val config: JsonObject): SignHandler {
  override fun handle(ctx: RoutingContext) {
    val paramsMap = ctx.queryParams()

    var params = ""
    for (pair in paramsMap) {
      val key = pair.key
      val value = pair.value
      params += "$key=$value"
    }

    val body = ctx.bodyAsString
    val method = ctx.request().method()

    val headers = HeadersMultiMap.headers()
    val sign = headers.get("sign")
    val nonce = headers.get("timestamp")
    val appKey = headers.get("appKey")
    val timestamp = headers.get("timestamp")

    if (sign.isEmpty() || nonce.isEmpty() || appKey.isEmpty() || timestamp.isEmpty()) {
      ctx.fail(400, Exception("签名错误"))
    }

    println("next")

//    return

    // 拿到原始的aes密钥
    val privateKey = config.getString("PRIVATE_KEY")
    val rawKey = RSA.decryptMessage(Base64.decode(appKey), privateKey)
    val specCode = config.getString("SPEC_CODE")

    if (!rawKey.contains(specCode)) {
      ctx.fail(400, Exception("key错误"))
    }

    // 验证签名
    var compareSign = ""
    if (method == HttpMethod.GET) {
      compareSign = MD5.digest("$params + $timestamp + $nonce + $rawKey".toByteArray()).base64
    }else if (method == HttpMethod.POST) {
      compareSign = MD5.digest("$body + $timestamp + $nonce + $rawKey".toByteArray()).base64
    }
    if (compareSign != sign) {
      ctx.fail(400, Exception("签名错误"))
    }

    // 防重放
    val currentTimestamp = System.currentTimeMillis()
    val compareTimestamp = timestamp.toLong()
    if (currentTimestamp - compareTimestamp > config.getLong("NONCE_MIN_EXPIRED")) {
      ctx.fail(400, Exception("请求过期"))
    }

    CoroutineScope(vertx.dispatcher()).launch {
      val GET_REQUEST = jsonObjectOf(
        "ACTION" to "GET",
        "MESSAGE" to nonce
      )
      val cacheNonce = vertx.eventBus().request<String>(NonceVerticle::class.java.name, GET_REQUEST).await().body()
      if (cacheNonce.isNotEmpty()) {
        ctx.fail(400, Exception("请求无效"))
      } else {
        val SET_REQUEST = jsonObjectOf(
          "ACTION" to "SET",
          "MESSAGE" to nonce
        )
        vertx.eventBus().send(NonceVerticle::class.java.name, SET_REQUEST)
      }
    }

  }
}
