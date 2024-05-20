package com.opos.reviewboard.repository

import com.opos.reviewboard.domain.data.Review
import io.getquill.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*

trait ReviewRepository {
  def create(review: Review): Task[Review]
  def getById(id: Long): Task[Option[Review]]
  def getByCompanyId(id: Long): Task[List[Review]]
  def getByUserId(userId: Long): Task[List[Review]]
  def getAll: Task[List[Review]]
  def update(id: Long, op: Review => Review): Task[Review]
  def delete(id: Long): Task[Review]
}

case class ReviewRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends ReviewRepository {

  import quill.*

  inline given reviewSchema: SchemaMeta[Review]     = schemaMeta[Review]("reviews")
  inline given reviewInsertMeta: InsertMeta[Review] = insertMeta[Review](_.id, _.createdAt, _.updatedAt)
  inline given reviewUpdateMeta: UpdateMeta[Review] = updateMeta[Review](_.id, _.createdAt)

  override def create(review: Review): Task[Review] =
    run {
      query[Review]
        .insertValue(lift(review))
        .returning(r => r)
    }

  override def getById(id: Long): Task[Option[Review]] =
    run {
      query[Review]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getByCompanyId(id: Long): Task[List[Review]] =
    run {
      query[Review]
        .filter(_.companyId == lift(id))
    }

  override def getByUserId(userId: Long): Task[List[Review]] =
    run {
      query[Review]
        .filter(_.userId == lift(userId))
    }

  override def getAll: Task[List[Review]] =
    run {
      query[Review]
    }

  override def update(id: Long, op: Review => Review): Task[Review] = 
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Update failed. Review not found for ${id}"))
      updated <- run {
                   query[Review]
                     .filter(_.id == lift(id))
                     .updateValue(lift(op(current)))
                     .returning(r => r)
                 }
    } yield updated
  override def delete(id: Long): Task[Review]                       =
    run {
      query[Review]
        .filter(_.id == lift(id))
        .delete
        .returning(r => r)
    }
}

object ReviewRepositoryLive {
  val layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase]]
    } yield new ReviewRepositoryLive(quill)
  }
}
