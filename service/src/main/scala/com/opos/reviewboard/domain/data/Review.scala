package com.opos.reviewboard.domain.data

import java.time.Instant
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class Review(
  id: Long, // PK
  companyId: Long,
  userId: Long,    // FK
  management: Int, // 1-5
  culture: Int,    // 1-5
  salary: Int,
  benefits: Int,
  wouldRecommend: Int,
  review: String,
  createdAt: Instant,
  updatedAt: Instant
)

object Review {
  given codec: JsonCodec[Review] = DeriveJsonCodec.gen[Review]
}
