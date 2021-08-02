package com.example.starter.middleware.impl

import com.example.starter.middleware.SignHandler
import com.example.starter.util.RSA
import com.example.starter.verticle.NonceVerticle
import com.example.starter.verticle.SSLVerticle
import com.soywiz.krypto.MD5
import com.soywiz.krypto.encoding.Base64
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.impl.headers.HeadersMultiMap
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.nio.charset.Charset

/**
 * 验证请求合法性
 */
class SignHandlerImpl(private val vertx: Vertx, private val config: JsonObject): SignHandler {
  override fun handle(ctx: RoutingContext) {
    val paramsMap = ctx.queryParams()

    var params = emptyList<String>()
    for (pair in paramsMap) {
      val key = pair.key
      val value = pair.value
      params = params.plus("$key=$value")
    }
    val paramsUrlFormat = params.joinToString("&")

    val body = ctx.bodyAsString
    val method = ctx.request().method()

    val headers = ctx.request().headers()

    val sign = headers.get("sign")
    val nonce = headers.get("nonce")
    val appKey = headers.get("appKey")
    val timestamp = headers.get("timestamp")
    val sessionId = headers.get("sessionId")

    if (sign.isEmpty() || nonce.isEmpty() || appKey.isEmpty() || timestamp.isEmpty()) {
      ctx.fail(400, Exception("签名错误"))
      return
    }

    CoroutineScope(vertx.dispatcher()).launch {
      // 拿到原始的aes密钥
      val privateKey = config.getString("PRIVATE_KEY")
      val specCode = config.getString("SPEC_CODE")
      val rawBase64Key = if (sessionId.isNullOrEmpty()) {
        RSA.decryptMessage(Base64.decode(appKey), privateKey)
      } else {
        // 如果sessionId不为空, 说明会话未过期, 直接从redis中获取key, 跳过RSA解密
        val GET_REQUEST = jsonObjectOf(
          "ACTION" to "GET",
          "SESSION_ID" to sessionId
        )

        val key: String? = vertx.eventBus().request<JsonObject>(SSLVerticle::class.java.name, GET_REQUEST).await().body().getString("key")
        if (key.isNullOrEmpty()) {
            // 如果在请求的过程中, sessionId缓存提前失效或不存在, 则使用RSA解密
          RSA.decryptMessage(Base64.decode(appKey), privateKey)
        } else {
          key
        }
      }

      // 验证签名
      var compareSign = ""
      if (method == HttpMethod.GET) {
        compareSign = MD5.digest("$paramsUrlFormat + $timestamp + $nonce + $rawBase64Key + $specCode".toByteArray(Charset.forName("UTF-8"))).hexUpper
      }else if (method == HttpMethod.POST) {
        compareSign = MD5.digest("$body + $timestamp + $nonce + $rawBase64Key + $specCode".toByteArray(Charset.forName("UTF-8"))).hexUpper
      }
      if (compareSign != sign) {
        ctx.fail(400, Exception("签名错误"))
        return@launch
      }

      // 防重放
      // 检查时间戳
      val currentTimestamp = System.currentTimeMillis()
      val compareTimestamp = timestamp.toLong()
      if (currentTimestamp - compareTimestamp > config.getLong("NONCE_MIN_EXPIRED_UNIT_SEC")*1000) {
        ctx.fail(400, Exception("请求过期"))
        return@launch
      }

      // 检查nonce值, 每个合法请求只能被调用一次
      val GET_REQUEST = jsonObjectOf(
        "ACTION" to "GET",
        "MESSAGE" to nonce
      )
      val isExist = vertx.eventBus().request<JsonObject>(NonceVerticle::class.java.name, GET_REQUEST).await().body().getBoolean("isExist")

      if (isExist) {
        ctx.fail(400, Exception("请求无效"))
      } else {
        val SET_REQUEST = jsonObjectOf(
          "ACTION" to "SET",
          "MESSAGE" to nonce
        )
        vertx.eventBus().send(NonceVerticle::class.java.name, SET_REQUEST)
        ctx.next()
      }

    }
  }
}
