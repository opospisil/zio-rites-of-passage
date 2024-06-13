package com.opos.reviewboard.http.requests

final case class LoginRequest(
  email: String,
  password: String
) derives zio.json.JsonCodec
