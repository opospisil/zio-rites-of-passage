package com.opos.reviewboard.http.endpoints

import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait CompanyEndpoints {
  val createEndpoint = endpoint
    .tag("companies")
    .name("create")
    .description("Create a new company")
    .in("companies")
    .post
    .in(jsonBody[CreateCompanyRequest])
    .out(jsonBody[Company])

  val getAllEndpoint = endpoint
    .tag("companies")
    .name("getAll")
    .description("Get all companies")
    .in("companies")
    .get
    .out(jsonBody[List[Company]])

  val getByIdEndpoint = endpoint
    .tag("companies")
    .name("getById")
    .description("Get a company by id")
    .in("companies" / path[String]("id"))
    .get
    .out(jsonBody[Option[Company]])

}
