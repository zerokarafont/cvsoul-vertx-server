package com.example.starter.controller.app

import com.example.starter.constant.TagAPI
import com.example.starter.service.app.TagAppService
import com.example.starter.util.coroutineHandler
import com.example.starter.util.jsonWithExceptionHandle
import com.example.starter.util.requestEventbusPayload
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.json.schema.SchemaParser
import io.vertx.kotlin.coroutines.await

suspend fun tagApp(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)

  router
    .get("/all")
    .produces("application/json")
    .coroutineHandler { ctx ->
      val message = requestEventbusPayload(action = TagAPI.ALL)

      val result = vertx.eventBus().request<JsonObject>(TagAppService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  return router
}
