package ru.yudnikov._trash.backend

import akka.actor.{ActorSystem, Props}
import akka.actor.ActorSystem
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn
import com.typesafe.config.ConfigFactory
import ru.yudnikov._trash.backend.actors.Monitor



object Backend extends App {

  val config = ConfigFactory.load()
  implicit val actorSystem: ActorSystem = ActorSystem("ClusterSystem", config)
  actorSystem.actorOf(Props(classOf[Monitor]))

}
