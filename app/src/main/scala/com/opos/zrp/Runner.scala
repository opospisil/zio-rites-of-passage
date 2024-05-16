package com.opos.zrp

import sttp.tapir.*
import zio.*
import zio.http.Server
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import com.opos.zrp.http.controllers.HealthController
import com.opos.zrp.http.HttpApi

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
        Server.default
      )
}
