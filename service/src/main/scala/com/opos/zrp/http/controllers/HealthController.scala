package com.opos.zrp.http.controllers

import zio.*
import com.opos.zrp.http.endpoints.HealthEndpoint
import sttp.tapir.server.ServerEndpoint

class HealthController private extends BaseController with HealthEndpoint {

  val health: ServerEndpoint[Any, Task] = healthEndpoint
    .serverLogicSuccess[Task](_ => ZIO.succeed("All good"))

  override def routes: List[ServerEndpoint[Any, Task]] = List(health)

}

object HealthController {
  val makeZIO = ZIO.succeed(new HealthController)
}
