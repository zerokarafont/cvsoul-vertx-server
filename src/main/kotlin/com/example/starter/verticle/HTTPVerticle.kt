package com.example.starter.verticle

import com.example.starter.controller.app.cateApp
import com.example.starter.controller.app.quoteApp
import com.example.starter.controller.app.tagApp
import com.example.starter.controller.app.userApp
import com.example.starter.controller.common.auth
import com.example.starter.middleware.SSLHandler
import com.example.starter.middleware.SignHandler
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.*
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.SchemaRouter
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.json.schema.schemaRouterOptionsOf

class HTTPVerticle : CoroutineVerticle() {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun start() {
    val schemaParser = SchemaParser.createDraft201909SchemaParser(
      SchemaRouter.create(vertx, schemaRouterOptionsOf())
    )

    val router = Router.router(vertx)

    router
      .route()
      .handler(BodyHandler.create().setBodyLimit(config.getLong("BODY_LIMIT")))
//      .handler(CSRFHandler.create(vertx, config.getString("CSRF_SECRET")))
      .handler(XFrameHandler.create(XFrameHandler.DENY))
      .handler(CSPHandler.create().addDirective("default-src", "self"))
      .handler(CorsHandler.create())
//      .handler(HSTSHandler.create())
      .handler(ResponseContentTypeHandler.create())
      .handler(ResponseTimeHandler.create())
      .handler(LoggerHandler.create())
      .handler(SignHandler.create(vertx, config))
      .handler(SSLHandler.create(vertx, config))
      .failureHandler { ctx ->
        val statusCode = ctx.statusCode()
        val message = ctx.failure().message
        val cause = ctx.failure().cause
        val method = ctx.request().method().name()

        if (statusCode == 400) {
          logger.error("[业务异常]: $message", cause)
          ctx.json(
            jsonObjectOf (
              "statusCode" to statusCode,
              "msg" to message,
              "data" to null
            )
          )
        } else if (statusCode == 401) {
          ctx.json(
            jsonObjectOf (
              "statusCode" to statusCode,
              "msg" to "请登录",
              "method" to method,
              "data" to null
            )
          )
        } else {
          logger.error("[未知路由异常]: $message", cause)
          ctx.json(
            jsonObjectOf(
              "statusCode" to 500,
              "msg" to "服务器错误",
              "data" to null
            )
          )
        }
      }

    router.mountSubRouter("/auth", auth(vertx, schemaParser))
    router.mountSubRouter("/app/user", userApp(vertx, schemaParser))
    router.mountSubRouter("/app/cate", cateApp(vertx, schemaParser))
    router.mountSubRouter("/app/tag", tagApp(vertx, schemaParser))
    router.mountSubRouter("/app/quote", quoteApp(vertx, schemaParser))

    try {
      vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(9000)
        .await()
      logger.info("HTTP客户端启动成功")
    } catch (e: Throwable) {
      val message = e.message
      val cause = Throwable(this::class.java.name)
      throw Error(message, cause)
    }

  }

}


