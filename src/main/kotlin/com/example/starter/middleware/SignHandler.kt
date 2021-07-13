package com.example.starter.middleware

import com.example.starter.middleware.impl.SignHandlerImpl
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

interface SignHandler: Handler<RoutingContext> {
  companion object {
    fun create(vertx: Vertx, config: JsonObject): SignHandlerImpl { return SignHandlerImpl(vertx, config) }
  }
}
