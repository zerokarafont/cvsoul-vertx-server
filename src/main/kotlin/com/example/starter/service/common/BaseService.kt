package com.example.starter.service.common

import com.example.starter.util.responseException
import com.example.starter.util.responseOk
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import org.jetbrains.annotations.Nullable

/**
 * 封装通用的CRUD
 */
open class BaseService(private val client: MongoClient, private val collectionName: String) : CoroutineVerticle() {

  protected suspend fun count(params: JsonObject?, query: JsonObject?): Long {
    val combineQuery = jsonObjectOf()
    params?.onEach { (k, v) ->
        when (v) {
          is String -> {
            if (v.isNotEmpty()) {
              combineQuery.put(k, v)
            }
          }
          else -> {
            if (v != null) {
              combineQuery.put(k, v)
            }
          }
        }
    }
    val mergeQuery = combineQuery.mergeIn(query ?: jsonObjectOf())
    return client.count(collectionName.lowercase(),mergeQuery).await() ?: 0
  }

  protected suspend fun findOne(params: JsonObject, query: JsonObject?, fields: JsonObject?): JsonObject {
    val id = params.getString("_id")
    val combineQuery = jsonObjectOf("_id" to id)
    val mergeQuery = combineQuery.mergeIn(query ?: jsonObjectOf())
    val resp = client.findOne(collectionName.lowercase(), mergeQuery, fields ?: jsonObjectOf()).await()
        ?: return responseException(msg = "不存在的文档")
    return responseOk(data = resp)
  }

  protected suspend fun removeOne(params: JsonObject): JsonObject {
    val id = params.getString("_id")
    val query = jsonObjectOf("_id" to id)
    client.findOneAndDelete(collectionName.lowercase(), query).await()
        ?: return responseException(msg = "删除失败, 不存在的文档")
    return responseOk(msg = "删除成功")
  }

  protected suspend fun updateOne(body: JsonObject): JsonObject {
    val id = body.getString("_id")
    val query = jsonObjectOf("_id" to id)
    client.findOneAndUpdate(collectionName.lowercase(), query, body).await()
      ?: return responseException(msg = "更新失败, 不存在的文档")
    return responseOk(msg = "更新成功")
  }

  protected suspend fun createOne(body: JsonObject): JsonObject {
    return try {
      client.insert(collectionName.lowercase(), body).await()
      responseOk(msg = "新增成功")
    }catch (e: Throwable) {
      responseException(msg = "新增失败, ${e.message}")
    }
  }

  protected suspend fun fetchPaginationList(
    params: JsonObject,
    query: JsonObject?,
    fields: JsonObject?,
    sort: JsonObject?,
  ): JsonObject {

    var page = 0
    var pageSize = 0

    val combineQuery = jsonObjectOf()
    params.onEach { (k, v) ->
      when (k) {
        "page" -> page = (v as Number).toInt()
        "pageSize" -> pageSize = (v as Number).toInt()
        else -> when (v) {
          is String -> {
            if (v.isNotEmpty()) {
              combineQuery.put(k, v)
            }
          }
          else -> {
            if (v != null) {
              combineQuery.put(k, v)
            }
          }
        }
      }
    }

    if (page == 0 || pageSize == 0) {
      return responseException(msg = "分页参数异常")
    }

    // query默认为空json
    val mergeQuery = combineQuery.mergeIn(query ?: jsonObjectOf())

    val options = FindOptions(
      jsonObjectOf(
        "fields" to (fields ?: jsonObjectOf()), // fields默认为空json
        "sort" to (sort ?: jsonObjectOf()), // sort默认为空json
        "limit" to pageSize,
        "skip" to (page - 1) * pageSize
      )
    )

    val resp = client.findWithOptions(collectionName.lowercase(), mergeQuery, options).await()
    val paginationResp = jsonObjectOf(
      "page" to page,
      "pageSize" to pageSize,
      "data" to resp
    )

    return responseOk(data = paginationResp)
  }
}
