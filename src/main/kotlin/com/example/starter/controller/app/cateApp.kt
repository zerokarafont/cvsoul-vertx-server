package com.example.starter.controller.app

import com.example.starter.service.CateService
import com.example.starter.service.UserService
import com.example.starter.util.coroutineHandler
import com.example.starter.util.jsonWithExceptionHandle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.json.schema.SchemaParser
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await

suspend fun cateApp(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)

  router
    .get("/all")
    .produces("application/json")
    .coroutineHandler { ctx ->
      val message = jsonObjectOf(
        "ACTION" to "ALL",
        "DATA" to null
      )

      val result = vertx.eventBus().request<JsonObject>(CateService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  return router
}
