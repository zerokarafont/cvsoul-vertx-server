package com.example.starter.controller.app

import com.example.starter.constant.VoiceAPI
import com.example.starter.service.app.VoiceAppService
import com.example.starter.util.*
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.intSchema
import io.vertx.json.schema.common.dsl.Schemas.stringSchema
import io.vertx.kotlin.coroutines.await

suspend fun voiceApp(vertx: Vertx, schemaParser: SchemaParser, config: JsonObject): Router {
  val router = Router.router(vertx)

  /**
   * 分页获取语录集封面列表
   */
  router
    .get("/playlist")
    .produces("application/json")
    .handler(
      ValidationHandler
        .builder(schemaParser)
        .queryParameter(param("page", intSchema()))
        .queryParameter(param("pageSize", intSchema()))
        .queryParameter(optionalParam("title", stringSchema()))
        .build()
    ).coroutineHandler { ctx ->
      val (params, body) = extractRequestContent(ctx)

      val sessionId = ctx.request().headers().get("sessionId")
      val appKey = ctx.request().headers().get("appKey")

      val message = requestEventbusPayload(
        action = VoiceAPI.FETCH_VOICE_PAGINATION_LIST,
        params = params,
        body = body
      )

      val result = vertx.eventBus().request<JsonObject>(VoiceAppService::class.java.name, message).await().body()
      val resp = encryptResponseData(result, vertx, sessionId, appKey, config)
      ctx.jsonWithExceptionHandle(resp)
    }

  return router
}
