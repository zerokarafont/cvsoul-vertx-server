package com.example.starter.util

import io.vertx.core.Vertx
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.web.handler.JWTAuthHandler

object JWT {
  private var handler: JWTAuthHandler? = null
  private var jwtAuth: JWTAuth? = null

  fun create(vertx: Vertx): JWT {
    if (handler == null || jwtAuth == null) {
      val jwtAuthConfig = JWTAuthOptions().setJWTOptions(JWTOptions().setExpiresInMinutes(7 * 24 * 60))
      val jwt = JWTAuth.create(vertx, jwtAuthConfig)
      jwtAuth = jwt
      handler = JWTAuthHandler.create(jwt)
    }

    return this
  }

  fun getHandler(): JWTAuthHandler? {
    return this.handler
  }

  fun getAuth(): JWTAuth? {
    return this.jwtAuth
  }
}
