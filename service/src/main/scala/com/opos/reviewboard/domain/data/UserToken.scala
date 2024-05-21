package com.opos.reviewboard.domain.data

final case class UserToken(
  email: String,
  token: String,
  expiration: Long
)
