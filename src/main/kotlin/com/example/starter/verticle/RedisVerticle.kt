package com.example.starter.verticle

import io.vertx.core.CompositeFuture
import io.vertx.core.DeploymentOptions
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.redis.client.Redis
import io.vertx.redis.client.RedisAPI
import io.vertx.redis.client.RedisClientType
import io.vertx.redis.client.RedisOptions

class RedisVerticle: CoroutineVerticle() {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun start() {
    val options = RedisOptions()
      .setType(RedisClientType.STANDALONE)
      .addConnectionString("redis://localhost:6379")

    try {
      val client = Redis
        .createClient(vertx, options)
        .connect()
        .await()
      logger.info("连接Redis成功")

      val redis = RedisAPI.api(client)

      CompositeFuture.all(
        vertx.deployVerticle(NonceVerticle(redis), DeploymentOptions().setConfig(config)),
        vertx.deployVerticle(SSLVerticle(redis), DeploymentOptions().setConfig(config))
      ).await()

    } catch (e: Throwable) {
      val message = e.message
      val cause = Throwable(this::class.java.name)
      throw Error(message, cause)
    }
  }
}
