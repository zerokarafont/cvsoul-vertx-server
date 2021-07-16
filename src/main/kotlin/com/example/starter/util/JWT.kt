package com.example.starter.util

import io.vertx.core.Vertx
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.handler.JWTAuthHandler

object JWT {
  private var handler: JWTAuthHandler? = null

  fun create(vertx: Vertx): JWTAuthHandler? {
    if (handler == null) {
      val jwtAuthConfig = JWTAuthOptions().setJWTOptions(JWTOptions().setExpiresInMinutes(7 * 24 * 60))
      val jwt = JWTAuth.create(vertx, jwtAuthConfig)
      handler = JWTAuthHandler.create(jwt)
    }

    return handler
  }
}
