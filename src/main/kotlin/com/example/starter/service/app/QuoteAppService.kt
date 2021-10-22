package com.example.starter.service.app

import com.example.starter.constant.CollectionSchema
import com.example.starter.constant.QuoteAPI
import com.example.starter.service.common.BaseService
import com.example.starter.util.responseOk
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.core.json.jsonObjectOf
import kotlinx.coroutines.launch

class QuoteAppService(private val client: MongoClient): BaseService(client, CollectionSchema.QUOTE_ALBUM.name) {

  override suspend fun start() {
    vertx.eventBus().consumer<JsonObject>(this::class.java.name).handler { message ->
      val action = message.body().getString("ACTION")
      val params = message.body().getJsonObject("PARAMS")
      val body = message.body().getJsonObject("BODY")

      when(action) {
        QuoteAPI.FETCH_QUOTE_ALBUM_PAGINATION_LIST.name -> launch { message.reply(fetchQuoteAlbumPaginationList(params)) }
        QuoteAPI.FETCH_QUOTE_ALBUM_PLAYLIST_DETAIL.name -> launch { message.reply(fetchQuoteAlbumPlaylistDetail(params)) }
      }
    }
  }

  private suspend fun fetchQuoteAlbumPlaylistDetail(params: JsonObject): JsonObject {
    println("service")

    val id = params.getString("_id")
    val match = jsonObjectOf(
      "_id" to id,
      "isOpen" to true,
    )
    val lookup = jsonObjectOf(
      "from" to CollectionSchema.QUOTE_ALBUM_N_TO_N_VOICE.name.lowercase(),
      "localField" to "_id",
      "foreignField" to "quoteId",
      "as" to "join"
    )
//    val resp = client.aggregate(CollectionSchema.QUOTE_ALBUM.name.lowercase(), jsonArrayOf(
//      jsonObjectOf("\$match" to match),
//      jsonObjectOf("\$lookup" to lookup)
//    ))

//    println("resp: $resp")

    return responseOk(data = jsonObjectOf(
      "_id" to "3e",
      "cateId" to "r43wqr",
      "user" to jsonObjectOf("username" to "fda"),
      "title" to "fda",
      "tags" to listOf<String?>(),
      "voices" to listOf<String?>()
    ))
  }

  private suspend fun fetchQuoteAlbumPaginationList(params: JsonObject): JsonObject {
    val query  = jsonObjectOf("isOpen" to true)
    val fields = jsonObjectOf("cover" to 1, "title" to 1)
    val sort   = jsonObjectOf()
    val title: String?  = params.getString("title")
    if (title?.isNotEmpty() == true) {
      query.put("title", jsonObjectOf("\$regex" to title))
    }
    return super.fetchPaginationList(params = params, query = query, fields = fields, sort = sort)
  }
}
