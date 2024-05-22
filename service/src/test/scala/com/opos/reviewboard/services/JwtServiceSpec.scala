package com.opos.reviewboard.services

import com.opos.reviewboard.config.JwtConfig
import com.opos.reviewboard.domain.data.User
import com.opos.reviewboard.syntax.*
import zio.*
import zio.test.*

object JwtServiceSpec extends ZIOSpecDefault {

  val service = ZIO.serviceWithZIO[JwtService]

  override def spec =
    suite("JwtServiceSpec")(
      test("generate and validate token") {
        for {
          service   <- ZIO.service[JwtService]
          userToken <- service.createToken(User(1L, "user@email.com", "password"))
          userId    <- service.verifyToken(userToken.token)
        } yield assertTrue(
          userId.id == 1L &&
            userId.email == "user@email.com"
        )
      }
    ).provide(
      JwtServiceLive.layer,
      ZLayer.succeed(JwtConfig("secret", 3600))
    )
}
