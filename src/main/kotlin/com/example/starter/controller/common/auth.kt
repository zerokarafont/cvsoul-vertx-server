package com.example.starter.controller.common

import com.example.starter.constant.AuthAPI
import com.example.starter.service.common.AuthService
import com.example.starter.util.coroutineHandler
import com.example.starter.util.extractRequestContent
import com.example.starter.util.jsonWithExceptionHandle
import io.vertx.core.Vertx
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
      val (params, body) = extractRequestContent(ctx)

      val sessionId = ctx.request().headers().get("sessionId")
      val appKey = ctx.request().headers().get("appKey")

      val message = jsonObjectOf(
        "ACTION" to AuthAPI.REGISTER,
        "BODY" to body,
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
      val (params, body) = extractRequestContent(ctx)

      val sessionId = ctx.request().headers().get("sessionId")
      val appKey = ctx.request().headers().get("appKey")

      val message = jsonObjectOf(
        "ACTION" to AuthAPI.LOGIN,
        "BODY" to body,
        "SESSION_ID" to sessionId,
        "APP_KEY" to appKey
      )

      val result = vertx.eventBus().request<JsonObject>(AuthService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  return router
}
