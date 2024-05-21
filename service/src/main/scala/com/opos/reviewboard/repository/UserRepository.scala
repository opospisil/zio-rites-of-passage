package com.opos.reviewboard.repository

import com.opos.reviewboard.domain.data.User
import zio.*
import io.getquill.*
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill

trait UserRepository {

  def create(user: User): Task[User]
  def getById(id: Long): Task[Option[User]]
  def getByEmail(email: String): Task[Option[User]]
  def update(id: Long, op: User => User): Task[User]
  def delete(id: Long): Task[Unit]

}

class UserRepositoryLive private (quill: Quill.Postgres[SnakeCase]) extends UserRepository {

  import quill.*

  inline given userSchema: SchemaMeta[User]     = schemaMeta[User]("users")
  inline given userInsertMeta: InsertMeta[User] = insertMeta[User](_.id)
  inline given userUpdateMeta: UpdateMeta[User] = updateMeta[User](_.id)

  override def create(user: User): Task[User] =
    run {
      query[User]
        .insertValue(lift(user))
        .returning(r => r)
    }

  override def getById(id: Long): Task[Option[User]] =
    run {
      query[User]
        .filter(_.id == lift(id))
    }.map(_.headOption)

  override def getByEmail(email: String): Task[Option[User]] =
    run {
      query[User]
        .filter(_.email == lift(email))
    }.map(_.headOption)

  override def update(id: Long, op: User => User): Task[User] =
    for {
      current <- getById(id).someOrFail(new RuntimeException(s"Update failed. User not found for ${id}"))
      updated <- run {
        query[User]
          .filter(_.id == lift(id))
          .updateValue(lift(op(current)))
          .returning(r => r)
      }
    } yield updated

  override def delete(id: Long): Task[Unit] =
    run {
      query[User]
        .filter(_.id == lift(id))
        .delete
    }.unit

}

object UserRepositoryLive {
  val layer = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[SnakeCase]]
    } yield new UserRepositoryLive(quill)
  }
}
