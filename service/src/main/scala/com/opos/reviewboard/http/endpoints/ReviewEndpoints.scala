package com.opos.reviewboard.http.endpoints

import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.http.requests.*
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*

trait ReviewEndpoints {

  // POST /reviews {CreateReviewRequest} => Review
  val createEndpoint = endpoint
    .tag("reviews")
    .name("create")
    .description("Create a new review")
    .in("reviews")
    .post
    .in(jsonBody[CreateReviewRequest])
    .out(jsonBody[Review])

  // GET /reviews => List[Review]
  val getAllEndpoint = endpoint
    .tag("reviews")
    .name("getAll")
    .description("Get all reviews")
    .in("reviews")
    .get
    .out(jsonBody[List[Review]])

  // GET /reviews/{id} => Option[Review]
  val getByIdEndpoint = endpoint
    .tag("reviews")
    .name("getById")
    .description("Get a review by id")
    .in("reviews" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Review]])

  // GET /reviews/company/{companyId} => List[Review]
  val getByCompanyIdEndpoint = endpoint
    .tag("reviews")
    .name("getByCompanyId")
    .description("Get reviews by company id")
    .in("reviews" / "company" / path[Long]("companyId"))
    .get
    .out(jsonBody[List[Review]])
}
