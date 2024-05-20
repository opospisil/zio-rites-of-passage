package com.opos.reviewboard.repository

import com.opos.reviewboard.Repository
import com.opos.reviewboard.domain.data.Review
import com.opos.reviewboard.syntax.*
import zio.*
import zio.internal.stacktracer.SourceLocation
import zio.test.*

object ReviewRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  override val initScript = "sql/reviews.sql"

  val goodReview = Review(
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

  val badReview = Review(
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

  override def spec =
    suite("ReviewRepositorySpec")(
      test("create review") {
        val program = for {
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
        } yield review

        program.assert { review =>
          review.management == goodReview.management &&
          review.culture == goodReview.culture &&
          review.salary == goodReview.salary &&
          review.benefits == goodReview.benefits &&
          review.wouldRecommend == goodReview.wouldRecommend &&
          review.review == goodReview.review
        }
      },
      test("Get by ids(id, companyId, userId)") {
        for {
          repo        <- ZIO.service[ReviewRepository]
          review      <- repo.create(goodReview)
          byId        <- repo.getById(review.id)
          byCompanyId <- repo.getByCompanyId(review.companyId)
          byUserId    <- repo.getByUserId(review.userId)
        } yield assertTrue(
          byId.contains(review) &&
            byCompanyId.contains(review) &&
            byUserId.contains(review)
        )
      },
      test("Get all") {
        for {
          repo        <- ZIO.service[ReviewRepository]
          review      <- repo.create(goodReview)
          review2     <- repo.create(badReview)
          byCompanyId <- repo.getByCompanyId(review.companyId)
          byUserId    <- repo.getByUserId(review.userId)
        } yield assertTrue(
          byCompanyId.toSet == Set(review, review2) &&
            byUserId.toSet == Set(review, review2)
        )
      },
      test("Edit review") {
        for {
          repo    <- ZIO.service[ReviewRepository]
          review  <- repo.create(goodReview)
          _ <- TestClock.adjust(1.seconds) // artificial delay to ensure updatedAt is different
          updated <- repo.update(review.id, _.copy(review = "Updated review"))
        } yield assertTrue(
          review.id == updated.id &&
            review.companyId == updated.companyId &&
            review.userId == updated.userId &&
            review.management == updated.management &&
            review.culture == updated.culture &&
            review.salary == updated.salary &&
            review.benefits == updated.benefits &&
            review.wouldRecommend == updated.wouldRecommend &&
            updated.review == "Updated review" &&
            review.createdAt == updated.createdAt &&
            review.updatedAt != updated.updatedAt
        )
      },
      test("Delete review") {
        for {
          repo   <- ZIO.service[ReviewRepository]
          review <- repo.create(goodReview)
          _      <- repo.delete(review.id)
          all    <- repo.getById(review.id)
        } yield assertTrue(
          all.isEmpty
        )
      }
    ).provide(
      ReviewRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )
}
