package com.opos.reviewboard.services

import collection.mutable
import com.opos.reviewboard.domain.data.Review
import com.opos.reviewboard.http.requests.CreateReviewRequest
import com.opos.reviewboard.repository.ReviewRepository
import com.opos.reviewboard.syntax.*
import zio.*
import zio.test.*

object ReviewServiceSpec extends ZIOSpecDefault {

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

  private val badReview = Review(
    id = 2L,
    companyId = 1L,
    userId = 1L,
    management = 1,
    culture = 1,
    salary = 1,
    benefits = 1,
    wouldRecommend = 1,
    review = "not a great company",
    createdAt = java.time.Instant.now(),
    updatedAt = java.time.Instant.now()
  )

  val service = ZIO.serviceWithZIO[ReviewService]

  val stubRepoLayer = ZLayer.succeed(
    new ReviewRepository {
      val db = mutable.Map.empty[Long, Review]

      override def create(Review: Review): Task[Review] =
        ZIO.succeed {
          val nextId    = db.keys.maxOption.getOrElse(0L) + 1
          val newReview = Review.copy(id = nextId)
          db += (nextId -> newReview)
          newReview
        }
      override def update(id: Long, op: Review => Review): Task[Review] =
        ZIO.attempt {
          val review = db(id)
          db += (id -> op(review))
          review
        }

      override def delete(id: Long): Task[Review] =
        ZIO.attempt {
          val review = db(id)
          db -= id
          review
        }

      override def getById(id: Long): Task[Option[Review]] =
        ZIO.succeed {
          id match {
            case 1 => Some(goodReview)
            case 2 => Some(badReview)
            case _ => None
          }
        }

      override def getByCompanyId(companyId: Long): Task[List[Review]] =
        ZIO.succeed {
          companyId match {
            case 1 => List(goodReview, badReview)
            case _ => List.empty
          }
        }

      override def getByUserId(userId: Long): Task[List[Review]] =
        ZIO.succeed {
          userId match {
            case 1 => List(goodReview, badReview)
            case _ => List.empty
          }
        }

      override def getAll: Task[List[Review]] =
        ZIO.succeed(List(goodReview, badReview))
    }
  )

  def spec = suite("ReviewServiceSpec")(
    test("create") {
      service(
        _.create(
          CreateReviewRequest(
            companyId = goodReview.companyId,
            management = goodReview.management,
            culture = goodReview.culture,
            salary = goodReview.salary,
            benefits = goodReview.benefits,
            wouldRecommend = goodReview.wouldRecommend,
            review = goodReview.review
          ),
          userId = 1L
        )
      ).assert { review =>
        review.companyId == goodReview.companyId &&
        review.management == goodReview.management &&
        review.culture == goodReview.culture &&
        review.salary == goodReview.salary &&
        review.benefits == goodReview.benefits &&
        review.wouldRecommend == goodReview.wouldRecommend &&
        review.review == goodReview.review
      }
    },
    test("Get by id") {
      for {
        byId     <- service(_.getById(1))
        notFound <- service(_.getById(3))
      } yield assertTrue(
        byId.contains(goodReview) &&
          notFound.isEmpty
      )
    },
    test("Get by company id") {
      for {
        byCompanyId <- service(_.getByCompanyId(1))
        notFound    <- service(_.getByCompanyId(3))
      } yield assertTrue(
        byCompanyId.toSet == Set(goodReview, badReview) &&
          notFound.isEmpty
      )
    },
    test("Get by user id") {
      for {
        byUserId <- service(_.getByUserId(1))
        notFound <- service(_.getByUserId(3))
      } yield assertTrue(
        byUserId.toSet == Set(goodReview, badReview) &&
          notFound.isEmpty
      )
    }
  ).provide(
    ReviewServiceLive.layer,
    stubRepoLayer
  )

}
