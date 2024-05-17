package com.opos.reviewboard.domain

import zio.*
import collection.mutable
import com.opos.reviewboard.domain.data.Company
import com.opos.reviewboard.http.requests.CreateCompanyRequest
//BUSINESS LOGIC
// in between the HTTP layer and the DB layer
trait CompanyService {

  def create(req: CreateCompanyRequest): Task[Company]

  def getAll: Task[List[Company]]

  def getById(id: Long): Task[Option[Company]]

  def getBySlug(slug: String): Task[Option[Company]]

}

class CompanyServiceDummy extends CompanyService {

  // in-memory db
  val db = mutable.Map.empty[Long, Company]

  override def create(req: CreateCompanyRequest): Task[Company] =
    ZIO.succeed {
      val newId      = db.keys.maxOption.getOrElse(0L) + 1
      val newCompany = req.toCompany(newId)
      db += (newId -> newCompany)
      newCompany
    }

  override def getAll: Task[List[Company]] = ZIO.succeed(db.values.toList)

  override def getById(id: Long): Task[Option[Company]] = ZIO.succeed(db.get(id))

  override def getBySlug(slug: String): Task[Option[Company]] =
    ZIO.succeed(db.values.find(_.slug == slug))

}


object CompanyService {
  val dummy= ZLayer.succeed(new CompanyServiceDummy)
}
