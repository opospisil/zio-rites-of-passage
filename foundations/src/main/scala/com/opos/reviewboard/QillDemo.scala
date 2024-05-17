package com.opos.zrp

import zio.*
import io.getquill.*
import io.getquill.jdbczio.Quill

object QillDemo extends ZIOAppDefault {

  val quillProgram = for {
    repo <- ZIO.service[JobRepository]
    _    <- repo.createJob(Job(-1, "SomeJobTitle", "https://www.google.com", "Google"))
    _    <- repo.createJob(Job(-1, "SomeOtherJobTitle", "https://www.google.com", "Google"))
  } yield ()

  override def run = quillProgram.provide(
    JobRepositoryLive.layer,
    Quill.Postgres.fromNamingStrategy(SnakeCase),
    Quill.DataSource.fromPrefix("mydbconf")
  )
}

trait JobRepository {
  def createJob(job: Job): Task[Job]
  def updateJob(id: Long, op: Job => Job): Task[Job]
  def deleteJob(id: Long): Task[Job]
  def getById(id: Long): Task[Option[Job]]
  def getAllJobs: Task[List[Job]]
}

class JobRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends JobRepository {

  import quill.*

  // scala 3 uses inline given with type.. scala 2 seems like it needs tobe private implicit without a type annotation
  inline given schema: SchemaMeta[Job]  = schemaMeta[Job]("jobs") // specify table name
  inline given insMeta: InsertMeta[Job] = insertMeta[Job](_.id)   // columns to be excluded in insert
  inline given upMeta: UpdateMeta[Job]  = updateMeta[Job](_.id)   // columns to be excluded in update

  override def createJob(job: Job): Task[Job] =
    run {
      query[Job]
        .insertValue(lift(job))
        .returning(j => j)
    }

  override def updateJob(id: Long, op: Job => Job): Task[Job] = for {
    current <- getById(id).someOrFail(new RuntimeException(s"Job not found for ${id}"))
    updated <- run {
                 query[Job]
                   .filter(_.id == lift(id))
                   .updateValue(lift(op(current)))
                   .returning(j => j)
               }
  } yield updated

  override def deleteJob(id: Long): Task[Job] =
    run {
      query[Job]
        .filter(_.id == lift(id))
        .delete
        .returning(j => j)
    }

  override def getById(id: Long): Task[Option[Job]] =
    run {
      query[Job]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getAllJobs: Task[List[Job]] =
    run(query[Job])
}

object JobRepositoryLive {
  val layer = ZLayer {
    ZIO.service[Quill.Postgres[SnakeCase]].map(new JobRepositoryLive(_))
  }
}
