package com.opos.reviewboard.config

final case class JwtConfig(
  secret: String,
  ttl: Long
)
