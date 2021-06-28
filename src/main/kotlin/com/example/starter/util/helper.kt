package com.example.starter.util

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Route.coroutineHandler(requestHandler: suspend (RoutingContext) -> Unit) {
  handler { ctx ->
    CoroutineScope(ctx.vertx().dispatcher()).launch {
      try {
        requestHandler(ctx)
      }catch (e: Throwable) {
        ctx.fail(400, e)
      }
    }.invokeOnCompletion {
      it?.run { ctx.fail(it) }
    }
  }
}
