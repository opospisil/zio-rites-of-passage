package com.opos.reviewboard.services

import collection.mutable
import com.opos.reviewboard.domain.data.Company
import com.opos.reviewboard.http.requests.CreateCompanyRequest
import com.opos.reviewboard.repository.CompanyRepository
import com.opos.reviewboard.syntax.*
import zio.*
import zio.test.*

object CompanyServiceSpec extends ZIOSpecDefault {

  val service = ZIO.serviceWithZIO[CompanyService]

  val stubRepoLayer = ZLayer.succeed(
    new CompanyRepository {
      val db = mutable.Map.empty[Long, Company]

      override def create(Company: Company): Task[Company] =
        ZIO.succeed {
          val nextId     = db.keys.maxOption.getOrElse(0L) + 1
          val newCompany = Company.copy(id = nextId)
          db += (nextId -> newCompany)
          newCompany
        }
      override def update(id: Long, op: Company => Company): Task[Company] =
        ZIO.attempt {
          val company = db(id)
          db += (id -> op(company))
          company
        }

      override def delete(id: Long): Task[Company] =
        ZIO.attempt {
          val company = db(id)
          db -= id
          company
        }

      override def getById(id: Long): Task[Option[Company]] =
        ZIO.succeed(db.get(id))

      override def getBySlug(slug: String): Task[Option[Company]] =
        ZIO.succeed(db.values.find(_.slug == slug))

      override def getAll: Task[List[Company]] =
        ZIO.succeed(db.values.toList)

    }
  )

  def spec = suite("CompanyServiceSpec")(
    test("create") {
      val companyZIO = service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))

      companyZIO.assert { company =>
        company.name == "Rock the JVM" &&
        company.slug == "rock-the-jvm" &&
        company.url == "rockthejvm.com"
      }
    },
    test("getBy Id") {
      //create
      //fetch
      val program = for {
        company <- service(_.create(CreateCompanyRequest("Rock the JVM", "rockthejvm.com")))
        fetched <- service(_.getById(company.id))
      } yield (fetched, company)

      program.assert { case (fetched, created) =>
        fetched.contains(created)
      }
    }
  ).provide(CompanyServiceLive.layer, stubRepoLayer)
}
