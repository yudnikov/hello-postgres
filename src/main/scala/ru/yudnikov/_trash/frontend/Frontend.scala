package ru.yudnikov._trash.frontend

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import ru.yudnikov._trash.backend.actors.{Monitor, Worker}

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Frontend extends App {

  val config = ConfigFactory.load()
  val actorSystem: ActorSystem = ActorSystem("ClusterSystem", config)
  actorSystem.actorOf(Props(classOf[Monitor]))

  def http(): Unit = {

    val config = ConfigFactory.load("frontend.conf")

    val hostname = "192.168.1.48"
    val port = 8080

    implicit val actorSystem: ActorSystem = ActorSystem("Frontend", config)
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher

    val route =
      get {
        pathSingleSlash {
          redirect(Uri(s"welcome"), StatusCodes.SeeOther)
        } ~
          path("welcome") {
            complete("Welcome!")
          } ~
          pathPrefix("ball") {
            pathEnd {
              val actor = actorSystem.actorOf(Props(classOf[Worker]))
              actor ! "Hello, from http!"
              complete("/ball")
            } ~
              path(IntNumber) { int =>
                complete(if (int % 2 == 0) "even ball" else "odd ball")
              }
          }
      } ~ post {
        pathSingleSlash {
          println("catched @ /")
          complete("done")
        } ~ path("calc" / "bonus") {
          entity(as[String]) { str =>
            println(str)
            complete("bonus clac")
          }
        }
      }

    val binding = Http().bindAndHandle(route, hostname, port)
    println(s"Online at http://$hostname:$port/" + "\n" + "press <Enter> to quit...")
    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  }

  http()

}
