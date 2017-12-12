package ru.yudnikov.superkassa

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, path, pathEnd, pathPrefix, pathSingleSlash, post, redirect}

import com.typesafe.config.ConfigFactory

import ru.yudnikov.superkassa.Messages.{JobRequest, RegisterBackend}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._


import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer


import scala.io.StdIn

class Frontend extends Actor {

  private var backends: Seq[ActorRef] = Seq()
  private var jobRequestCounter: Long = 0

  override def receive = {
    case RegisterBackend if !backends.contains(sender()) =>
      registerBackend(sender())
    case JobRequest =>
      if (backends.nonEmpty) {
        val n = count
        val m = backends.length
        val i = n % m
        backends(i.toInt) forward JobRequest(n)
      } else {
        println("No backends registered My Lord!")
      }
  }

  def registerBackend(actorRef: ActorRef): Unit = {
    context watch sender()
    backends = backends :+ sender()
  }

  def count: Long = {
    jobRequestCounter = jobRequestCounter + 1
    jobRequestCounter
  }

}

object Frontend extends App {
  val clusterSystemName = "Cluster"
  val clusterConfig = args match {
    case Array(hostname) =>
      ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname = ${"\"" + hostname + "\""}")
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
        .withFallback(ConfigFactory.load())
    case Array(clusterHostname, clusterPort, faceHostname, facePort) =>
      face(faceHostname, facePort)
      ConfigFactory.parseString(s"akka.remote.netty.tcp.port = $clusterPort")
        .withFallback(ConfigFactory.parseString(s"akka.remote.netty.tcp.hostname = ${"\"" + clusterHostname + "\""}"))
        .withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]"))
        .withFallback(ConfigFactory.load())
  }
  val clusterActorSystem: ActorSystem = ActorSystem(clusterSystemName, clusterConfig)
  implicit val clusterExecutionContext: ExecutionContext = clusterActorSystem.dispatcher
  val frontend = clusterActorSystem.actorOf(Props(classOf[Frontend]), name = "frontend")
  clusterActorSystem.actorOf(Props(classOf[Monitor]))
  clusterActorSystem.scheduler.schedule(1 second, 2 second, frontend, JobRequest)

  def face(hostname: String, port: String): Unit = {

    val appName = "Frontend"
    val config = ConfigFactory.load(ConfigFactory.parseResources("frontend.conf"))

    implicit val actorSystem = ActorSystem(appName, config)
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = actorSystem.dispatcher

    val route =
      get {
        pathSingleSlash {
          redirect(Uri(s"welcome"), StatusCodes.SeeOther)
        } ~
          path("welcome") {
            complete("Welcome to Agency Commission Calculator!")
          } ~
          path("calc") {
            complete("specify calculator!")
          } ~
          path("calc" / "bonus") {
            complete("Bonus Caclulator")
          } ~
          path("foo") {
            complete("/foo")
          } ~
          path("foo" / "bar") {
            complete("/foo/bar")
          } ~
          pathPrefix("ball") {
            pathEnd {
              complete("/ball")
            } ~
              path(IntNumber) { int: Int =>
                complete(if (int % 2 == 0) "even ball" else "odd ball")
              }
          }
      } ~ post {
        pathSingleSlash {
          println("catched @ /")
          complete("done")
        } ~ path ("calc" / "bonus") {
          entity(as[String]) { str =>
            println(str)
            complete("bonus clac")
          }
        }
      }

    val binding = Http().bindAndHandle(route, hostname, port.toInt)
    println(s"$appName online at http://$hostname:$port/" + "\n" + "press Enter to quit...")
//    StdIn.readLine()
//    binding.flatMap(_.unbind()).onComplete(_ => actorSystem.terminate())
  }

}
