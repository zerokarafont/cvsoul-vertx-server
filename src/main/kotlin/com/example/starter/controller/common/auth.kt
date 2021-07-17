package com.example.starter.controller.common

import com.example.starter.service.AuthService
import com.example.starter.util.coroutineHandler
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
              .property("username", stringSchema())
              .property("password", stringSchema())
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
