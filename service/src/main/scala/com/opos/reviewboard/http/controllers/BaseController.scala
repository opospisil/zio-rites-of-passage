package com.opos.reviewboard.http.controllers

import zio.*
import sttp.tapir.server.ServerEndpoint

trait BaseController {
  def routes: List[ServerEndpoint[Any, Task]]
}
