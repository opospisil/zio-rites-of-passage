package com.opos.reviewboard.http.requests

final case class UpdatePasswordRequest(
  email: String,
  oldPassword: String,
  newPassword: String
) derives zio.json.JsonCodec
