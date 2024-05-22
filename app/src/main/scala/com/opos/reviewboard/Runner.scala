package com.opos.reviewboard

import com.opos.reviewboard.http.HttpApi
import com.opos.reviewboard.http.controllers.HealthController
import com.opos.reviewboard.repository.*
import com.opos.reviewboard.services.*
import sttp.tapir.*
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import zio.*
import zio.http.Server

object Runner extends ZIOAppDefault {

  val serverProgram = for {
    endpoints <- HttpApi.endpointsZIO
    _ <- Server.serve(
           ZioHttpInterpreter(
             ZioHttpServerOptions.default
           ).toHttp(endpoints)
         )

  } yield ()

  override def run =
    serverProgram
      .provide(
        Server.default,
        // services
        CompanyServiceLive.layer,
        ReviewServiceLive.layer,
        UserServiceLive.layer,
        JwtServiceLive.configuredLayer,
        // repositories
        CompanyRepositoryLive.layer,
        ReviewRepositoryLive.layer,
        UserRepositoryLive.layer,
        Repository.dataLayer
      )
}
