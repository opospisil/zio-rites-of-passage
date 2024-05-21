package com.opos.reviewboard.http.controllers

import com.opos.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.*
import zio.*
import com.opos.reviewboard.domain.errors.HttpError

class HealthController private extends BaseController with HealthEndpoint {

  val health: ServerEndpoint[Any, Task] = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

    val error: ServerEndpoint[Any, Task] = 
      errorEndpoint
    .serverLogic[Task](_ => ZIO.fail(new RuntimeException("Jeb!")).either)

  override def routes: List[ServerEndpoint[Any, Task]] = List(health, error)

}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
