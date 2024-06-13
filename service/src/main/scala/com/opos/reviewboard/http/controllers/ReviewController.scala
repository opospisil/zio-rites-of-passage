package com.opos.reviewboard.http.controllers

import com.opos.reviewboard.http.endpoints.ReviewEndpoints
import com.opos.reviewboard.services.ReviewService
import sttp.tapir.server.ServerEndpoint
import zio.*

class ReviewController private (service: ReviewService) extends BaseController with ReviewEndpoints {

  val create: ServerEndpoint[Any, Task] =
    createEndpoint.serverLogic(service.create(_, -1L).either) // TODO add userID once user management is implemented

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic(service.getById(_).either)

  val getByCompanyId: ServerEndpoint[Any, Task] = getByCompanyIdEndpoint.serverLogic(service.getByCompanyId(_).either)

  override def routes: List[ServerEndpoint[Any, Task]] = List(create, getById, getByCompanyId)

}

object ReviewController {
  def makeZIO = for {
    service <- ZIO.service[ReviewService]
  } yield new ReviewController(service)
}
