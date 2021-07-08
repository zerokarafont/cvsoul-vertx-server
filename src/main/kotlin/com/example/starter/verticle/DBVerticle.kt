package com.example.starter.verticle

import com.example.starter.service.RestService
import io.vertx.core.CompositeFuture
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await

class DBVerticle : CoroutineVerticle() {
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

    val query = JsonObject()
    client.find("cvsoul", query) { res ->
      if (res.succeeded()) {
        for (json in res.result()) {
          println(json.encodePrettily())
        }
      } else {
        res.cause().printStackTrace()
      }
    }

    CompositeFuture.all(
      vertx.deployVerticle(RestService(client)),
      vertx.deployVerticle(RestService(client)),
    ).await()
  }
}
