package com.example.starter.verticle

import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class WSVerticle : CoroutineVerticle() {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun start() {
    val router = Router.router(vertx)
    router.route()
      .handler(CorsHandler.create().allowCredentials(true).addOrigin("*"))
      .failureHandler { ctx ->
        logger.error("socket error: ${ctx.failure().message}")
      }

    // 方式一 需要使用配套的sockjs client 客户端
//    val options = SockJSHandlerOptions()
//      .setHeartbeatInterval(2000)
//      .setRegisterWriteHandler(true)
//
//    val socketJSHandler = SockJSHandler.create(vertx, options)

//    router.mountSubRouter("/socket", socketJSHandler.socketHandler { socket ->
//      println("socket mounted")
//      val writeHandlerID = socket.writeHandlerID()
//
//      socket
//        .handler { buffer -> socket.write(buffer) }
//        .drainHandler { _ -> }
//        .endHandler {  }
//        .exceptionHandler { e -> logger.error(e.message) }
//
//    })

    try {
      vertx
        .createHttpServer()
//        .requestHandler(router)
        // 方式二 原生websockets
        .webSocketHandler { serverWebsockets ->
          serverWebsockets.handler(serverWebsockets::write)
        }
        .listen(14000)
        .await()
      println("websockets server is running on port 8001")
    } catch (e: Throwable) {
      val message = e.message
      val cause = Throwable(this::class.java.name)
      logger.error(message, cause)
      throw Error(message, cause)
    }
  }
}
