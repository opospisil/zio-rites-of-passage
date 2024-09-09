package com.opos.reviewboard.services

import com.opos.reviewboard.domain.data.Review
import com.opos.reviewboard.http.requests.CreateReviewRequest
import com.opos.reviewboard.repository.ReviewRepository
import zio.*

trait ReviewService {

  def create(req: CreateReviewRequest, userId: Long): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(companyId: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
}

class ReviewServiceLive private (repo: ReviewRepository) extends ReviewService {

  override def create(req: CreateReviewRequest, userId: Long): Task[Review] =
    repo.create(req.toReview(userId))

  override def getById(id: Long): Task[Option[Review]] = repo.getById(id)

  override def getByCompanyId(companyId: Long): Task[List[Review]] = repo.getByCompanyId(companyId)

  override def getByUserId(userId: Long): Task[List[Review]] = repo.getByUserId(userId)

}

object ReviewServiceLive {
  val layer = ZLayer {
    for {
      repo <- ZIO.service[ReviewRepository]
    } yield new ReviewServiceLive(repo)
  }
}
