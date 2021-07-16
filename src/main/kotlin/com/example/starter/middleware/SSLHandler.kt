package com.example.starter.middleware

import com.example.starter.middleware.impl.SSLHandlerImpl
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

interface SSLHandler: Handler<RoutingContext> {
  companion object {
    fun create(vertx: Vertx, config: JsonObject): SSLHandlerImpl { return SSLHandlerImpl(vertx, config) }
  }
}
