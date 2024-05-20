package com.opos.reviewboard.repository

import com.opos.reviewboard.domain.data.Company
import io.getquill.*
import io.getquill.jdbczio.Quill
import zio.*

trait CompanyRepository {
  def create(Company: Company): Task[Company]
  def update(id: Long, op: Company => Company): Task[Company]
  def delete(id: Long): Task[Company] // Some if deleted, None if not found
  def getById(id: Long): Task[Option[Company]]
  def getBySlug(slug: String): Task[Option[Company]]
  def getAll: Task[List[Company]]
}

class CompanyRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends CompanyRepository {

  import quill.*

  inline given schema: SchemaMeta[Company]  = schemaMeta[Company]("companies") // specify table name
  inline given insMeta: InsertMeta[Company] = insertMeta[Company](_.id)        // columns to be excluded in insert
  inline given upMeta: UpdateMeta[Company]  = updateMeta[Company](_.id)        // columns to be excluded in update

  override def create(company: Company): Task[Company] =
    run {
      query[Company]
        .insertValue(lift(company)) //lift is a Quill macro that lifts data values into the query
        .returning(c => c)
    }

  override def update(id: Long, op: Company => Company): Task[Company] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Update failed. Company not found for ${id}"))
    updated <- run {
                 query[Company]
                   .filter(_.id == lift(id))
                   .updateValue(lift(op(current)))
                   .returning(c => c)
               }
  } yield updated

  override def delete(id: Long): Task[Company] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .delete
        .returning(c => c)
    }

  override def getById(id: Long): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.id == lift(id))
        .take(1)
    }.map(_.headOption)

  override def getBySlug(slug: String): Task[Option[Company]] =
    run {
      query[Company]
        .filter(_.slug == lift(slug))
        .take(1)
    }.map(_.headOption)

  override def getAll: Task[List[Company]] =
    run {
      query[Company]
    }
}

object CompanyRepositoryLive {
  val layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase]]
    } yield new CompanyRepositoryLive(quill)
  }
}

object CompanyRepositoryRunner extends ZIOAppDefault {

  val quillTest = for {
    repo <- ZIO.service[CompanyRepository]
    _    <- repo.create(Company(-1L, "test-company", "Test Company", "test-company.com"))
  } yield ()
  override def run = quillTest
    .provide(
      CompanyRepositoryLive.layer,
      Quill.Postgres.fromNamingStrategy(SnakeCase),
      Quill.DataSource.fromPrefix("reviewboard.db")
    )

}
