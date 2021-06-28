package com.example.starter.service

import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgPool
import kotlinx.coroutines.launch

class RestService(private var pool: PgPool) : CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<String>(this::class.java.name).handler { message ->
        message.reply("get")
    }
  }
}
