package com.example.starter.verticle

import com.example.starter.service.AuthService
import com.example.starter.service.CateService
import com.example.starter.service.QuoteService
import com.example.starter.service.UserService
import io.vertx.core.CompositeFuture
import io.vertx.core.DeploymentOptions
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class DBVerticle : CoroutineVerticle() {

  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun start() {
    val dbConfig = jsonObjectOf(
      "useObjectId" to true,
      "db_name" to config.getString("DB"),
      "host" to config.getString("HOST"),
      "port" to config.getInteger("PORT"),
      "username" to config.getString("USER"),
      "password" to config.getString("PASSWORD"),
      "authMechanism" to "SCRAM-SHA-256"
    )
    val client = MongoClient.createShared(vertx, dbConfig)

    logger.info("连接MongoDB成功")

    CompositeFuture.all(
      vertx.deployVerticle(AuthService(client), DeploymentOptions().setConfig(config)),
      vertx.deployVerticle(UserService(client), DeploymentOptions().setConfig(config)),
      vertx.deployVerticle(CateService(client), DeploymentOptions().setConfig(config)),
      vertx.deployVerticle(QuoteService(client), DeploymentOptions().setConfig(config)),
    ).await()
  }
}
