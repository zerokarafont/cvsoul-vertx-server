package com.example.starter.controller.app

import com.example.starter.constant.QuoteAPI
import com.example.starter.service.app.QuoteAppService
import com.example.starter.util.coroutineHandler
import com.example.starter.util.jsonWithExceptionHandle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.validation.RequestParameters
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.intSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.await

suspend fun quoteApp(vertx: Vertx, schemaParser: SchemaParser): Router {
  val router = Router.router(vertx)

  /**
   * 分页获取语录集封面列表
   */
  router
    .get("/playlist/display")
    .produces("application/json")
    .handler(
      ValidationHandler
        .builder(schemaParser)
        .queryParameter(param("page", intSchema()))
        .queryParameter(param("pageSize", intSchema()))
        .queryParameter(optionalParam("cateId", stringSchema()))
        .queryParameter(optionalParam("title", stringSchema()))
        .build()
    ).coroutineHandler { ctx ->
      val params = ctx.get<RequestParameters>(ValidationHandler.REQUEST_CONTEXT_KEY)

      val page = params.queryParameter("page").integer
      val pageSize = params.queryParameter("pageSize").integer
      val cateId = params.queryParameter("cateId")?.string
      val title = params.queryParameter("title")?.string

      val sessionId = ctx.request().headers().get("sessionId")
      val appKey = ctx.request().headers().get("appKey")

      val message = jsonObjectOf(
        "ACTION" to QuoteAPI.FETCH_QUOTE_ALBUM_PAGINATION_LIST,
        "PARAMS" to jsonObjectOf(
          "page" to page,
          "pageSize" to pageSize,
          "cateId" to cateId,
          "title" to title
        ),
        "SESSION_ID" to sessionId,
        "APP_KEY" to appKey
      )

      val result = vertx.eventBus().request<JsonObject>(QuoteAppService::class.java.name, message).await().body()
      ctx.jsonWithExceptionHandle(result)
    }

  return router
}
