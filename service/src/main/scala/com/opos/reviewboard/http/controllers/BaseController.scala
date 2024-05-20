package com.opos.reviewboard.http.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*

trait BaseController {
  def routes: List[ServerEndpoint[Any, Task]]
}
