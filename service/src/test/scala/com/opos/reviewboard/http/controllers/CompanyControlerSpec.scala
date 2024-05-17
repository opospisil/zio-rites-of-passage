package com.opos.reviewboard.http.controllers

import zio.test.*
import zio.*
import zio.json.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.generic.auto
import sttp.client3.testing.SttpBackendStub
import sttp.client3.*
import sttp.monad.MonadError
import sttp.tapir.ztapir.RIOMonadError
import com.opos.reviewboard.http.requests.CreateCompanyRequest
import com.opos.reviewboard.domain.data.Company
import com.opos.reviewboard.syntax.*
import sttp.tapir.server.ServerEndpoint
import com.opos.reviewboard.domain.CompanyService

object CompanyControlerSpec extends ZIOSpecDefault {

  private val testCompany = Company(1, "test-company-inc.", "Test Company inc.", "test.com")
  private val serviceStub = new CompanyService {
    override def create(req: CreateCompanyRequest): Task[Company] =
      ZIO.succeed(testCompany)
    override def getAll: Task[List[Company]] =
      ZIO.succeed(List(testCompany))
    override def getById(id: Long): Task[Option[Company]] =
      ZIO.succeed(if (id == 1) Some(testCompany) else None)
    override def getBySlug(slug: String): Task[Option[Company]] =
      ZIO.succeed(if (slug == testCompany.slug) Some(testCompany) else None)
  }

  private given zioMonadError: MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO(endpointFn: CompanyController => ServerEndpoint[Any, Task]) = for {
    // create the controller
    controllerZIO <- CompanyController.makeZIO
    // build tapir backend
    backendStub <- ZIO.succeed {
                     TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
                       .whenServerEndpointRunLogic(endpointFn(controllerZIO))
                       .backend()
                   }
  } yield backendStub

  override def spec =
    suite("CompanyControlerSpec")(
      test("simple test") {
        assertZIO(ZIO.succeed(1 + 1))(
          Assertion.assertion("basic math")(_ == 2)
        )
      },
      test("post company") {
        val program = for {
          backendStub <- backendStubZIO(_.create)
          response <- basicRequest
                        .post(uri"/companies")
                        .body(CreateCompanyRequest("Test Company inc.", "test.com").toJson)
                        .send(backendStub)

        } yield response.body

        // inspect the response
        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[Company].toOption) // Option[Company]
            .contains(Company(1, "test-company-inc.", "Test Company inc.", "test.com"))
        }
      },
      test("get all compaines") {
        val program = for {
          backendStub <- backendStubZIO(_.getAll)
          response <- basicRequest
                        .get(uri"/companies")
                        .send(backendStub)

        } yield response.body
        // inspect the response
        program.assert { respBody =>
          respBody.toOption
            .flatMap(_.fromJson[List[Company]].toOption) // Option[Company]
            .contains(List(testCompany))
        }
      }
    ).provide(ZLayer.succeed(serviceStub))
}
