package com.opos.reviewboard.http.requests

import com.opos.reviewboard.domain.data.Review
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class CreateReviewRequest(
  companyId: Long,
  management: Int,
  culture: Int,
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String
) {
  def toReview(userId: Long): Review = Review(
    id = -1L,
    companyId = companyId,
    userId = userId,
    management = management,
    culture = culture,
    salary = salary,
    benefits = benefits,
    wouldRecommend = wouldRecommend,
    review = review,
    createdAt = java.time.Instant.now(),
    updatedAt = java.time.Instant.now()
  )
}

object CreateReviewRequest {
  given codec: JsonCodec[CreateReviewRequest] = DeriveJsonCodec.gen[CreateReviewRequest]
}
