package api.gen

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import api.gen.HttpServer.bindingFuture
import api.gen.sample.Users.{UsersHandler, UsersResource}
import api.gen.sample.definitions.{Problem, User}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class HttpServer {

  implicit val system: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty, "my-server-system")
  implicit val executionContext: ExecutionContextExecutor =
    system.executionContext

  var db: Map[String, User] =
    Seq(User("0001", "User1"), User("0002", "User2")).map(e => e.id -> e).toMap

  val userRoutes: Route = UsersResource.routes(new UsersHandler {
    override def listUsers(respond: UsersResource.ListUsersResponse.type)(
        limit: Option[Int]
    ): Future[UsersResource.ListUsersResponse] = Future.successful {
      respond.OK(db.values.toVector)
    }

    override def createUser(respond: UsersResource.CreateUserResponse.type)(
        body: User
    ): Future[UsersResource.CreateUserResponse] = Future.successful {
      db += (body.id -> body)
      respond.Created
    }

    override def getUser(respond: UsersResource.GetUserResponse.type)(
        userId: String
    ): Future[UsersResource.GetUserResponse] = Future.successful {
      db.get(userId) match {
        case Some(user) => respond.OK(user)
        case None =>
          respond.NotFound(Problem(Some(404), StatusCodes.NotFound.reason))
      }
    }
  })

  val routes: Route = Route.seal(userRoutes)

  def start(): Future[Http.ServerBinding] = {
    val bindingFuture: Future[Http.ServerBinding] =
      Http().newServerAt("localhost", 1234).bind(routes)

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
    }))

    bindingFuture
  }

}

object HttpServer extends App {

  val httpServer = new HttpServer()

  val bindingFuture: Future[Http.ServerBinding] = httpServer.start()

  val binding = Await.result(bindingFuture, Duration.Inf)
  println(s"Server now online. Please navigate to ${binding.localAddress}")

}
