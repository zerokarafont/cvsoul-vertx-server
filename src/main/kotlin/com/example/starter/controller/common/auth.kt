package com.example.starter.controller.common

import com.example.starter.service.AuthService
import com.example.starter.util.coroutineHandler
import com.example.starter.util.decryptKeyDirectOrFromCache
import com.example.starter.util.encryptData
import com.example.starter.util.jsonWithExceptionHandle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.validation.RequestParameters
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.objectSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await

suspend fun auth(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)

//  router
//    .get("/test")
//    .produces("application/json")
//    .coroutineHandler { ctx ->
//      val sessionId = ctx.request().headers().get("sessionId")
//      val appKey = ctx.request().headers().get("appKey")
//      val rawData = Json.encode(listOf("1", "2"))
//      val key = decryptKeyDirectOrFromCache(vertx, sessionId, appKey, config = jsonObjectOf("PRIVATE_KEY" to "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAKtYV1y+C36rg047aRxcfvO70SuZ3v901uDNdZtc6OS4xMJUqCSuxYri2wgW65JVu15byHrhadNHbuMCptBBSafrQfGLb9zC8eb1e1sGCLPDiiR00PnDlxhi1yBrYUhuh7rMM5Q2oiXXQ890hLi5vT3f2G3JOC/IESO4PNtZoOVDAgMBAAECgYEAmihR+VPWnbGL4k/bYrPpWDprl5HJqwYg6YKQxCYUNuvxwnFOEirX+fveWmncqfzOJbfoKH4zqu4C2uUg1g9XRu3esPfRc4LGhymPhkNh7Tl24NZQM3Aq5HWMLNRpjf6czSw5boWe+hc0zijwduVWH1ffAGnIVCsbn1ej3kI5xvECQQDffeK7FB14NcpT7YGPozhaeeB/fBDiexxTWH/xE2XGxcHhnqJRu1TMgSpfkqY30F+JPqpxEHxPyThuJBxHTpgJAkEAxESufa6KyWUtmjfNTUDq/ZRvMlCrPi07QsWSsu+PXznelXJwKM7edQUKfOEEz3AKoMgSCej0dpoClxmptKPt6wJBALwhvDq/NA99OFRuGiJPw6Bl+BVY2t3LxIxkc078hTBOSGckon8qsrd0A7dwO3vAbKublN6Yggyn1ljhDOz5vAECQELHA39rlXj0XdGszsNMJSfmCvdZjwn2pcUQJ9uNuoAx1lOOvi6ERBgYgPsALHzPqC4QJGMOya5YtCzo5F67r8UCQQCHoZKeciKkgk4NgVVNubEUJmwkS7ZRPoTV/zZzyb48ppIIZIPkSbzNPdTw9GQiablzfyXK6U3rppRWhtPwV6L0"))
//      val testData = encryptData(rawData, key)
//
//      ctx.json(jsonObjectOf(
//        "statusCode" to 200,
//        "msg" to "测试",
//        "data" to testData
//      ))
//    }
  /**
   * 注册
   */
  router
    .post("/register")
    .produces("application/json")
    .handler(
      ValidationHandler
        .builder(schemaParser)
        .body(
          Bodies.json(
            objectSchema()
              .requiredProperty("username", stringSchema())
              .requiredProperty("password", stringSchema())
              .requiredProperty("confirmPass", stringSchema())
              .requiredProperty("code", stringSchema()) // 邀请码
          )
        )
        .build()
    )
    .coroutineHandler { ctx ->
      val params = ctx.get<RequestParameters>(ValidationHandler.REQUEST_CONTEXT_KEY)
      val body = params.body().jsonObject

      val sessionId = ctx.request().headers().get("sessionId")
      val appKey = ctx.request().headers().get("appKey")

      val message = jsonObjectOf(
        "ACTION" to "REGISTER",
        "DATA" to body,
        "SESSION_ID" to sessionId,
        "APP_KEY" to appKey
      )

      val result = vertx.eventBus().request<JsonObject>(AuthService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  /**
   * 登录
   */
  router
    .post("/login")
    .produces("application/json")
    .handler(
      ValidationHandler
        .builder(schemaParser)
        .body(
          Bodies.json(
            objectSchema()
              .requiredProperty("username", stringSchema())
              .requiredProperty("password", stringSchema())
          )
        )
        .build()
    ).coroutineHandler { ctx ->
      val params = ctx.get<RequestParameters>(ValidationHandler.REQUEST_CONTEXT_KEY)
      val body = params.body().jsonObject

      val sessionId = ctx.request().headers().get("sessionId")
      val appKey = ctx.request().headers().get("appKey")

      val message = jsonObjectOf(
        "ACTION" to "LOGIN",
        "DATA" to body,
        "SESSION_ID" to sessionId,
        "APP_KEY" to appKey
      )

      val result = vertx.eventBus().request<JsonObject>(AuthService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  return router
}
