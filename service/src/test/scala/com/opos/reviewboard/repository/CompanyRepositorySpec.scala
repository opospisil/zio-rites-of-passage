package com.opos.reviewboard.repository

import com.opos.reviewboard.Repository
import com.opos.reviewboard.Repository.dataSourceLayer
import com.opos.reviewboard.domain.data.Company
import com.opos.reviewboard.domain.data.Company.makeSlug
import com.opos.reviewboard.syntax.*
import java.sql.SQLException
import zio.*
import zio.test.*

object CompanyRepositorySpec extends ZIOSpecDefault with RepositorySpec {

  private val company = Company(1L, "test-company", "Test Company Inc.", "test.com")

  private val companyGenZIO = for {
    id      <- Gen.long(1, 1000).runHead.map(_.get)
    name    <- Gen.string(Gen.alphaChar).runHead.map(_.get)
    slug    <- ZIO.succeed(makeSlug(name))
    domain  <- Gen.string(Gen.alphaChar).runHead.map(_.get)
    company <- ZIO.succeed(Company(id, slug, name, domain))
  } yield company

  override def spec =
    suite("CompanyRepositorySpec")(
      test("Create company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(company)
        } yield company

        program.assert {
          case Company(_, "test-company", "Test Company Inc.", "test.com", _, _, _, _, _) => true
          case _                                                                          => false
        }
      },
      test("Create duplicate should fail") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(company)
          err     <- repo.create(company).flip
        } yield err

        program.assert {
          case _: SQLException => true
          case _               => false
        }
      },
      test("Get company by id and slug") {
        val program = for {
          repo        <- ZIO.service[CompanyRepository]
          company     <- repo.create(company)
          fetchById   <- repo.getById(company.id)
          fetchBySlug <- repo.getBySlug(company.slug)
        } yield (company, fetchById, fetchBySlug)

        program.assert { case (company, fetchById, fetchBySlug) =>
          fetchById.contains(company) && fetchBySlug.contains(company)
        }
      },
      test("Update company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(company)
          up      <- repo.update(company.id, _.copy(name = "New Name"))
          fetch   <- repo.getById(company.id)
        } yield (up, fetch)

        program.assert { case (up, fetch) =>
          fetch.contains(up)
        }
      },
      test("Delete company") {
        val program = for {
          repo    <- ZIO.service[CompanyRepository]
          company <- repo.create(company)
          _       <- repo.delete(company.id)
          fetch   <- repo.getById(company.id)
        } yield fetch

        program.assert(_.isEmpty)
      },
      test("Get all") {
        val program = for {
          repo      <- ZIO.service[CompanyRepository]
          companies <- ZIO.collectAllPar((1 to 10) map (_ => companyGenZIO.flatMap(repo.create)))

          all       <- repo.getAll
        } yield (companies, all)

        program.assert { case (companies, all) =>
          companies.toSet == all.toSet
        }
      }
    ).provide(
      CompanyRepositoryLive.layer,
      dataSourceLayer,
      Repository.quillLayer,
      Scope.default
    )

}
