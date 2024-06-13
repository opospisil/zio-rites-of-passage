package com.opos.reviewboard.http.requests

final case class DeleteUserRequest(
  email: String,
  password: String
) derives zio.json.JsonCodec
