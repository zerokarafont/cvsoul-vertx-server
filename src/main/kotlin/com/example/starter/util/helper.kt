package com.example.starter.util

import com.example.starter.verticle.SSLVerticle
import com.soywiz.krypto.AES
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.Base64
import com.soywiz.krypto.encoding.base64
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

fun Route.coroutineHandler(requestHandler: suspend (RoutingContext) -> Unit) {
  handler { ctx ->
    CoroutineScope(ctx.vertx().dispatcher()).launch {
      try {
        requestHandler(ctx)
      }catch (e: Throwable) {
        ctx.fail(400, e)
      }
    }.invokeOnCompletion {
      it?.run { ctx.fail(it) }
    }
  }
}

/**
 * 如果sessionId为 “” 或者 “update” 则返回false
 */
fun isRequestSessionIdValid(sessionId: String): Boolean {
  if (sessionId.isEmpty() || sessionId == "update") {
    return false
  }
  return true
}

/**
 * 解出原始的aseKey
 */
suspend fun decryptKeyDirectOrFromCache(
  vertx: Vertx,
  sessionId: String,
  appKey: String,
  config: JsonObject
): ByteArray {
  val rawBase64Key = if (sessionId.isEmpty()) {
    RSA.decryptMessage(Base64.decode(appKey), config.getString("PRIVATE_KEY"))
  } else {
    // 如果sessionId不为空, 说明会话未过期, 直接从redis中获取key, 跳过RSA解密
    val GET_REQUEST = jsonObjectOf(
      "ACTION" to "GET",
      "SESSION_ID" to sessionId
    )

    val key: String? =
      vertx.eventBus().request<JsonObject>(SSLVerticle::class.java.name, GET_REQUEST).await().body().getString("key")
    if (key.isNullOrEmpty()) {
      // 如果在请求的过程中, sessionId缓存提前失效或不存在, 则使用RSA解密
      RSA.decryptMessage(Base64.decode(appKey), config.getString("PRIVATE_KEY"))
    } else {
      key
    }
  }

  return Base64.decode(rawBase64Key)
}

/**
 * 使用key解出明文数据
 */
inline fun <reified T> decryptData(encryptData: String, key: ByteArray): T {
  val data = String(AES.decryptAes128Cbc(Base64.decode(encryptData), key, Padding.PKCS7Padding))

  return when (T::class) {
    Int::class -> data.toInt() as T
    Boolean::class -> data.toBoolean() as T
    JsonObject::class -> Json.decodeValue(data) as T
    List::class -> Json.decodeValue(data, List::class.java) as T
    else -> data as T
  }
}

/**
 * 使用key加密明文数据
 */
fun encryptData(rawData: String, key: ByteArray): String {
  return AES.encryptAes128Cbc(rawData.toByteArray(), key, Padding.PKCS7Padding).base64
}

fun RoutingContext.jsonWithExceptionHandle(result: JsonObject): Future<Void>? {
  val statusCode = result.getNumber("statusCode")
  val msg = result.getString("msg")
  if (statusCode != 200) {
    throw Exception(msg)
  }
  return this.json(result)
}

/**
 * 返回UTC+8 时区 (北京标准时间)
 * @return timestamp CST(北京标准时间)
 */
fun CSTTimestamp(): Long {
  val date = Date()
  return date.time / 1000
//  val calendar = Calendar.getInstance()
//  calendar.time = date
//  calendar.set(Calendar.HOUR, Calendar.HOUR + 8)
//  return calendar.timeInMillis / 1000
}
