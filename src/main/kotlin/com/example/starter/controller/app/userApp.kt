package com.example.starter.controller.app

import com.example.starter.constant.UserAPI
import com.example.starter.service.UserService
import com.example.starter.util.JWT
import com.example.starter.util.coroutineHandler
import com.example.starter.util.jsonWithExceptionHandle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.json.schema.SchemaParser
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await

suspend fun userApp(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)
  /**
   * 获取登录用户个人资料
   */
  router
    .get("/profile")
    .produces("application/json")
    .handler(JWT.create(vertx).getHandler())
    .coroutineHandler { ctx ->
      val user = ctx.user().principal()

      val message = jsonObjectOf(
        "ACTION" to UserAPI.Profile,
        "PARAMS" to user
      )

      val result = vertx.eventBus().request<JsonObject>(UserService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  return router
}
