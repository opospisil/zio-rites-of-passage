package com.opos.reviewboard.http

import com.opos.reviewboard.http.controllers.*

object HttpApi {

  def gatherRoutes(controllers: List[BaseController]) = controllers.flatMap(_.routes)

  def makeControllers = for {
    healthController  <- HealthController.makeZIO
    companyController <- CompanyController.makeZIO
  } yield List(healthController, companyController)

  val endpointsZIO = makeControllers.map(gatherRoutes)
}
