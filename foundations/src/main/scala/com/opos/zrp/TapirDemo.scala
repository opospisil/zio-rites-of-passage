package com.opos.zrp

import sttp.tapir._
import zio._
import zio.http.Server
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import sttp.tapir.server.ziohttp.ZioHttpServerOptions
import sttp.tapir.generic.auto._
import Job._
import CreateJobRequest._
import sttp.tapir.json.zio.jsonBody
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec
import sttp.tapir.server.ServerEndpoint

object TapirDemo extends ZIOAppDefault {

  val simplestEndpoint = endpoint
    .tag("Simplest")
    .name("Simplest")
    .description("The simplest endpoint you can imagine")
    .get
    .in("simplest")
    .out(plainBody[String])
    .serverLogicSuccess[Task](_ => ZIO.succeed("Tapir says henlo AF!"))

  val simpleServerProgram = Server.serve(
    ZioHttpInterpreter(
      ZioHttpServerOptions.default
    ).toHttp(simplestEndpoint)
  )

  val db = scala.collection.mutable.Map(
    1L -> Job(1L, "Instructor", "https://www.google.com", "Google")
  )

  val getAllJobsEndpoint: ServerEndpoint[Any,Task] = endpoint
    .tag("jobs")
    .name("Get all jobs")
    .description("Get all jobs from the database")
    .get
    .in("jobs")
    .out(jsonBody[List[Job]])
    .serverLogicSuccess[Task](_ => ZIO.succeed(db.values.toList))

  val createJobEndpoint: ServerEndpoint[Any, Task]= endpoint
    .tag("jobs")
    .name("Create a job")
    .description("Create a job in the database")
    .in("jobs")
    .post
    .in(jsonBody[CreateJobRequest])
    .out(jsonBody[Job])
    .serverLogicSuccess(req =>
      ZIO.succeed {
        val id  = db.keys.max + 1
        val job = Job(id, req.title, req.url, req.company)
        db.put(id, job)
        job
      }
    )

  val getByIdEndpoint: ServerEndpoint[Any, Task] = endpoint
    .tag("jobs")
    .name("Get job by id")
    .description("Get a job from the database by id")
    .in("jobs" / path[Long]("id"))
    .get
    .out(jsonBody[Option[Job]])
    .serverLogicSuccess(id => ZIO.succeed(db.get(id)))

  val fullServerProgram = Server.serve(
    ZioHttpInterpreter(
      ZioHttpServerOptions.default
    ).toHttp(
      List(
        getAllJobsEndpoint,
        createJobEndpoint,
        getByIdEndpoint
      )
    )
  )

  override def run = fullServerProgram.provide(
    Server.default
  )
}


case class CreateJobRequest(title: String, url: String, company: String)

object CreateJobRequest {
  implicit val createJobRequestCodec: JsonCodec[CreateJobRequest] = DeriveJsonCodec.gen[CreateJobRequest]
}
