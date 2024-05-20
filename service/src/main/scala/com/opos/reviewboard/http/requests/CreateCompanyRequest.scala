package com.opos.reviewboard.http.requests

import com.opos.reviewboard.domain.data.Company
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class CreateCompanyRequest(
  name: String,
  url: String,
  location: Option[String] = None,
  country: Option[String] = None,
  industry: Option[String] = None,
  image: Option[String] = None,
  tags: List[String] = List()
) {
  def toCompany(id: Long): Company = Company(
    id = id,
    slug = Company.makeSlug(name),
    name = name,
    url = url,
    location = location,
    country = country,
    industry = industry,
    image = image,
    tags = tags
  )
}

object CreateCompanyRequest {
  given codec: JsonCodec[CreateCompanyRequest] = DeriveJsonCodec.gen[CreateCompanyRequest]
}
