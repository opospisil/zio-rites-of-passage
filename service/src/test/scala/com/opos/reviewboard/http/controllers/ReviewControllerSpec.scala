package com.opos.reviewboard.http.controllers

import com.opos.reviewboard.domain.data.*
import com.opos.reviewboard.http.requests.*
import com.opos.reviewboard.services.ReviewService
import com.opos.reviewboard.syntax.*
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.monad.MonadError
import sttp.tapir.generic.auto
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.json.*
import zio.test.*

object ReviewControllerSpec extends ZIOSpecDefault {

  private val goodReview = Review(
    id = 1L,
    companyId = 1L,
    userId = 1L,
    management = 5,
    culture = 5,
    salary = 5,
    benefits = 5,
    wouldRecommend = 5,
    review = "Great company",
    createdAt = java.time.Instant.now(),
    updatedAt = java.time.Instant.now()
  )

  private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

  private val serviceStub = new ReviewService {

    override def create(req: CreateReviewRequest, userId: Long): Task[Review] =
      ZIO.succeed(goodReview)

    override def getById(id: Long): Task[Option[Review]] =
      ZIO.succeed(if (id == 1) Some(goodReview) else None)

    override def getByCompanyId(companyId: Long): Task[List[Review]] =
      ZIO.succeed {
        if (companyId == 1) List(goodReview)
        else List.empty
      }

    override def getByUserId(userId: Long): Task[List[Review]] =
      ZIO.succeed {
        if (userId == 1) List(goodReview)
        else List.empty
      }
  }

  private def backendStubZIO(endpointFn: ReviewController => ServerEndpoint[Any, Task]) = for {
    // create the controller
    controllerZIO <- ReviewController.makeZIO
    // build tapir backend
    backendStub <- ZIO.succeed {
                     TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
                       .whenServerEndpointRunLogic(endpointFn(controllerZIO))
                       .backend()
                   }
  } yield backendStub

  override def spec =
    suite("ReviewControllerSpec")(
      test("create") {
        for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
                        .post(uri"reviews")
                        .body(
                          CreateReviewRequest(
                            companyId = goodReview.companyId,
                            management = goodReview.management,
                            culture = goodReview.culture,
                            salary = goodReview.salary,
                            benefits = goodReview.benefits,
                            wouldRecommend = goodReview.wouldRecommend,
                            review = goodReview.review
                          ).toJson
                        )
                        .send(backendStub)
        } yield assertTrue {
          response.body.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
        }
      },
      test("get by id") {
        for {
          backendStub <- backendStubZIO(_.getById)
          response <- basicRequest
                        .get(uri"reviews/1")
                        .send(backendStub)
        } yield assertTrue {
          response.body.toOption
            .flatMap(_.fromJson[Review].toOption)
            .contains(goodReview)
        }
      },
      test("get by company id") {
        for {
          backendStub <- backendStubZIO(_.getByCompanyId)
          response <- basicRequest
                        .get(uri"reviews/company/1")
                        .send(backendStub)
          emptyResponse <- basicRequest
                             .get(uri"reviews/company/2")
                             .send(backendStub)
        } yield assertTrue {
          response.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List(goodReview)) &&
          emptyResponse.body.toOption
            .flatMap(_.fromJson[List[Review]].toOption)
            .contains(List.empty)
        }
      }
    ).provide(ZLayer.succeed(serviceStub))

}
