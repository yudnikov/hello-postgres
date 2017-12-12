package ru.yudnikov.examples.postgres

import java.sql.{Connection, ResultSet}

import org.postgresql.PGConnection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PostgresExample {

  private val databaseSettings = DatabaseSettings("192.168.1.42", 5432, "example", "postgres", "postgres")
  val connection: Connection with PGConnection = databaseSettings.postgresConnection

  def execute(query: String): Unit = {
    val statement = connection.createStatement()
    statement.execute(query)
  }

  def executeQuery[T](query: String)(transform: ResultSet => T): Future[Iterator[T]] = Future {
    val statement = connection.createStatement()
    val rs = statement.executeQuery(query)
    new Iterator[T] {
      override def hasNext: Boolean = rs.next()
      override def next(): T = transform(rs)
    }
  }
}
