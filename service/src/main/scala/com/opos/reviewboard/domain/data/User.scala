package com.opos.reviewboard.domain.data

final case class User(
  id: Long,
  email: String,
  hashPassword: String
) {
  def toUserId: UserId = UserId(id, email)
}

final case class UserId(
  id: Long,
  email: String
)
