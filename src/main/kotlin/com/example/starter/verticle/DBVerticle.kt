package com.example.starter.verticle

import com.example.starter.service.RestService
import io.vertx.core.CompositeFuture
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import java.util.*


class DBVerticle : CoroutineVerticle() {
  override suspend fun start() {
    val connectOptions = PgConnectOptions()
      .setPort(config.getInteger("PORT"))
      .setHost(config.getString("HOST"))
      .setDatabase(config.getString("DB"))
      .setUser(config.getString("USER"))
      .setPassword(config.getString("PASSWORD"))
      .setReconnectAttempts(2)
      .setReconnectInterval(1000)

    val poolOptions = PoolOptions().setMaxSize(5)

    val pool = PgPool.pool(vertx, connectOptions, poolOptions)

    CompositeFuture.all(
      vertx.deployVerticle(RestService(pool)),
      vertx.deployVerticle(RestService(pool)),
    ).await()
  }
}
