package com.opos.reviewboard.http.controllers

import collection.mutable
import com.opos.reviewboard.domain.data.Company
import com.opos.reviewboard.http.endpoints.CompanyEndpoints
import com.opos.reviewboard.services.CompanyService
import sttp.tapir.server.ServerEndpoint
import zio.*

class CompanyController private (service: CompanyService) extends BaseController with CompanyEndpoints {

  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogic { req =>
    service.create(req).either
  }

  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogic { _ =>
    service.getAll.either
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogic { id =>
    ZIO
      .attempt(id.toLong)
      .flatMap(service.getById)
      .catchSome { case _: NumberFormatException =>
        service.getBySlug(id) // try to get by slug if id is not a number
      }
      .either

  }

  override def routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)

}

object CompanyController {
  def makeZIO = for {
    service <- ZIO.service[CompanyService]
  } yield new CompanyController(service)
}
