package com.opos.reviewboard.http.controllers

import com.opos.reviewboard.domain.errors.HttpError
import com.opos.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint {

  val health: ServerEndpoint[Any, Task] =
    healthEndpoint.serverLogic(_ => ZIO.attempt("All good").either)

  val error: ServerEndpoint[Any, Task] =
    errorEndpoint.serverLogic(_ => ZIO.fail(new RuntimeException("Jeb!")).either)

  override def routes: List[ServerEndpoint[Any, Task]] = List(health, error)

}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
