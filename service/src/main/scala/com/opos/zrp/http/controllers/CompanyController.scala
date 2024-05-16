package com.opos.zrp.http.controllers

import zio.*
import com.opos.zrp.http.endpoints.CompanyEndpoints

import collection.mutable
import com.opos.zrp.domain.data.Company
import sttp.tapir.server.ServerEndpoint

class CompanyController extends BaseController with CompanyEndpoints {

  // in-memory db

  val db = mutable.Map.empty[Long, Company]

  val create: ServerEndpoint[Any, Task] = createEndpoint.serverLogicSuccess { req =>
    ZIO.succeed {
      val newId      = db.keys.maxOption.getOrElse(0L) + 1
      val newCompany = req.toCompany(newId)
      db += (newId -> newCompany)
      newCompany
    }
  }

  val getAll: ServerEndpoint[Any, Task] = getAllEndpoint.serverLogicSuccess { _ =>
    ZIO.succeed(db.values.toList)
  }

  val getById: ServerEndpoint[Any, Task] = getByIdEndpoint.serverLogicSuccess { id =>
    ZIO
      .attempt(id.toLong)
      .map(db.get)
  }

  override def routes: List[ServerEndpoint[Any, Task]] = List(create, getAll, getById)

}

object CompanyController {
  def makeZIO: ZIO[Any, Nothing, CompanyController] = ZIO.succeed(new CompanyController)
}
