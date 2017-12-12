package ru.yudnikov.examples.postgres

import java.sql.{Connection, DriverManager, ResultSet}
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import org.postgresql.PGConnection
import ru.yudnikov.examples.postgres.actors.{Poller, Spammer}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val tch_rules = PostgresBackend.executeQuery[(Int, String)]("SELECT * FROM tch_rules;") { rs =>
     rs.getInt("id") -> rs.getString("conditions")
  }

  val actorSystem = ActorSystem("backend")
  val poller = actorSystem.actorOf(Props(classOf[Poller]), "poller")
  val spammer = actorSystem.actorOf(Props(classOf[Spammer]), "spammer")

  println("Press <Enter> to terminate...")
  Console.in.read
  actorSystem.terminate

}
