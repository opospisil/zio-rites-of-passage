package com.opos.reviewboard.services

import zio.*
import collection.mutable
import com.opos.reviewboard.domain.data.Company
import com.opos.reviewboard.http.requests.CreateCompanyRequest
import com.opos.reviewboard.repository.CompanyRepository
//BUSINESS LOGIC
// in between the HTTP layer and the DB layer
// controller(http) -> service -> repository(db)
trait CompanyService {

  def create(req: CreateCompanyRequest): Task[Company]

  def getAll: Task[List[Company]]

  def getById(id: Long): Task[Option[Company]]

  def getBySlug(slug: String): Task[Option[Company]]

}

class CompanyServiceLive private (repo: CompanyRepository) extends CompanyService {

  override def create(req: CreateCompanyRequest): Task[Company] =
    repo.create(req.toCompany(-1L))

  override def getAll: Task[List[Company]] = repo.getAll

  override def getById(id: Long): Task[Option[Company]] = repo.getById(id)

  override def getBySlug(slug: String): Task[Option[Company]] = repo.getBySlug(slug)

}

object CompanyServiceLive {
  val layer = ZLayer {
    for {
      repo <- ZIO.service[CompanyRepository]
    } yield new CompanyServiceLive(repo)

  }
}
