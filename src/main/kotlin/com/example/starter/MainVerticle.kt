package com.example.starter

import com.example.starter.verticle.DBVerticle
import com.example.starter.verticle.HTTPVerticle
import com.example.starter.verticle.WSVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.impl.logging.LoggerFactory
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlin.system.exitProcess

class MainVerticle : CoroutineVerticle() {
  private val logger = LoggerFactory.getLogger(this::class.java)

  override suspend fun start() {
    vertx.exceptionHandler { e ->
      logger.error("unhandledThrowable: ${e.message}", e.cause)
      //做一些其他操作 尝试重启 同步状态 redis 集群通信等等
      // 发邮件通知崩溃事件

      // 以非0状态码退出配合docker的重启策略
      exitProcess(1)
    }

    // 全局配置
    val configBuffer = vertx.fileSystem().readFileBlocking("config.json")
    val configOptions = ConfigStoreOptions().setType("json").setConfig(JsonObject(configBuffer))
    val retrieverOptions = ConfigRetrieverOptions().addStore(configOptions)
    val retriever = ConfigRetriever.create(vertx, retrieverOptions)

    val config = retriever.config.await()

    try {
      // 先部署数据库
      vertx.deployVerticle(DBVerticle(), DeploymentOptions().setConfig(config)).await()
      // 再部署http server
      vertx.deployVerticle(HTTPVerticle(), DeploymentOptions().setConfig(config)).await()

      vertx.deployVerticle(WSVerticle()).await()

      logger.info("vertx部署成功")
    } catch (e: Throwable) {
      logger.error("vertx部署失败: ${e.message}", e.cause)
    }

  }
}
