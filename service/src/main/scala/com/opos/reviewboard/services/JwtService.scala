package com.opos.reviewboard.services

import com.auth0.jwt.*
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.opos.reviewboard.config.JwtConfig
import com.opos.reviewboard.domain.data.*
import java.time.Instant
import pureconfig.*
import zio.*

trait JwtService {
  def createToken(user: User): Task[UserToken]
  def verifyToken(token: String): Task[UserId]
}
// not a good idea to create clock when instantiating the service
// this will ensure that same clock is used for encoding and decoding
class JwtServiceLive private (jwtConfig: JwtConfig, clock: java.time.Clock) extends JwtService {
  private val ISSUER         = "issuer.com"
  private val CLAIM_USERNAME = "username"

  private val algorithm = Algorithm.HMAC512(jwtConfig.secret)

  private val verifier = JWT
    .require(algorithm)
    .withIssuer(ISSUER)
    .asInstanceOf[BaseVerification]
    .build(clock)

  override def createToken(user: User): Task[UserToken] = for {
    now <- ZIO.attempt(clock.instant())
    exp <- ZIO.succeed(now.plusSeconds(jwtConfig.ttl))
    token <- ZIO.attempt {
               JWT
                 .create()
                 .withIssuer(ISSUER)
                 .withIssuedAt(now)
                 .withExpiresAt(exp)
                 .withSubject(user.id.toString) // user identifier
                 .withClaim(CLAIM_USERNAME, user.email)
                 .sign(algorithm)
             }
  } yield UserToken(user.email, token, exp.getEpochSecond())

  override def verifyToken(token: String): Task[UserId] = for {
    decoded <- ZIO.attempt(verifier.verify(token))
    userId <- ZIO.attempt(
                UserId(decoded.getSubject().toLong, decoded.getClaim(CLAIM_USERNAME).asString())
              )
  } yield userId

}

object JwtServiceLive {

  inline given jwtReader: ConfigReader[JwtConfig] = ConfigReader.forProduct2("secret", "jwt_ttl")(JwtConfig(_, _))
  private val configLayer = ZLayer {
    ZIO.fromEither {
      ConfigSource.resources("application.conf").at("reviewboard.jwt").load[JwtConfig]
    }
  }

  val layer = ZLayer {
    for {
      clock  <- Clock.javaClock
      config <- ZIO.service[JwtConfig]
    } yield new JwtServiceLive(config, clock)
  }

  val configuredLayer = configLayer >>> layer
}

object JtwServiceDemo extends ZIOAppDefault {

  inline given jwtReader: ConfigReader[JwtConfig] = ConfigReader.forProduct2("secret", "jwt_ttl")(JwtConfig(_, _))
  val configLayer = ZLayer {
    ZIO.fromEither {
      ConfigSource.resources("application.conf").at("reviewboard.jwt").load[JwtConfig]
    }
  }

  val program = for {
    jwtService <- ZIO.service[JwtService]
    user       <- ZIO.succeed(User(1L, "user@email.com", "password"))
    token      <- jwtService.createToken(user)
    _          <- Console.printLine(s"Token: ${token.token}")
    userId     <- jwtService.verifyToken(token.token)
    _          <- Console.printLine(s"User Id: ${userId}")
  } yield ()

  override def run = program.provide(JwtServiceLive.layer, configLayer)
// eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9. header
// eyJzdWIiOiJzdWJqZWN0IiwiaXNzIjoiaXNzdWVyLmNvbSIsImV4cCI6MTcxNjI5MDI1NywiaWF0IjoxNzE2Mjg2NjU3LCJ1c2VybmFtZSI6InVzZXJAZW1haWwuY29tIn0.
// bo0xAU_zT81pKqDzm94zpL1NPLuMfdAbWbj_WRhHuEKiL1TuoK2nmlqTMKKrDf_d_9kBaBOURPhBgGEnQ6VDSg
}
