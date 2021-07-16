package com.example.starter.controller

import com.example.starter.service.RestService
import com.example.starter.util.JWT
import com.example.starter.util.coroutineHandler
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.validation.RequestParameters
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.intSchema
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await

suspend fun restRoute(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)

  router
    .get("/one")
    .produces("application/json")
    .handler(JWT.create(vertx))
    .handler(
      ValidationHandler
        .builder(schemaParser)
        .queryParameter(optionalParam("age", intSchema()))
        .build()
    ).coroutineHandler { ctx ->
      val params = ctx.get<RequestParameters>(ValidationHandler.REQUEST_CONTEXT_KEY)
      val age = params.queryParameter("age")?.integer

      val result = vertx.eventBus().request<String>(RestService::class.java.name, age).await().body()
      ctx.json(
        jsonObjectOf(
          "statusCode" to 200,
          "msg" to null,
          "data" to result,
        )
      )
    }

  return router
}
