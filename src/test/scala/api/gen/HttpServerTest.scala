package api.gen

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import api.gen.sample.definitions.User
import org.junit.runner.RunWith
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@RunWith(classOf[JUnitRunner])
class HttpServerTest extends AnyFunSuite with Matchers with ScalatestRouteTest with Implicits {

  val httpServer: HttpServer = new HttpServer()

  override protected def beforeAll(): Unit = Await.ready(httpServer.start(), 30.seconds)

  test("list all users") {
    Get("/users") ~> httpServer.routes ~> check {
      responseAs[Seq[User]] shouldBe httpServer.db.values.toSeq
    }
  }

  test("get one use") {
    Get("/users/0001") ~> httpServer.routes ~> check {
      responseAs[User] shouldBe httpServer.db.getOrElse("0001", fail("no user found"))
    }
  }

  test("add new user") {
    Post("/users", User("0003", "test-user")) ~> httpServer.routes ~> check {
      response.status shouldBe StatusCodes.Created
    }
    Get("/users/0003") ~> httpServer.routes ~> check {
      responseAs[User] shouldBe httpServer.db.getOrElse("0003", fail("no user found"))
    }
  }

}
