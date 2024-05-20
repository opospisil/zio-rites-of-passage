package com.opos.reviewboard.http.controllers

import com.opos.reviewboard.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint
import zio.*

class HealthController private extends BaseController with HealthEndpoint {

  val health: ServerEndpoint[Any, Task] = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  override def routes: List[ServerEndpoint[Any, Task]] = List(health)

}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
