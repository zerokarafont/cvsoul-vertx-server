package com.example.starter.service

import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.coroutines.CoroutineVerticle

class RestService(private var client: MongoClient) : CoroutineVerticle() {

  override suspend fun start() {
    vertx.eventBus().consumer<String>(this::class.java.name).handler { message ->
        message.reply("get")
    }
  }
}
