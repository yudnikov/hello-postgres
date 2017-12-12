package ru.yudnikov.examples.postgres

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object AsyncSelect extends App {

  val futureTableNames = PostgresBackend.executeQuery[String] {
    "SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';"
  } {
    _ getString 1
  }

  val futureTables = futureTableNames.flatMap { tableNames =>
    Future.sequence {
      tableNames.map { tableName =>
        val futureTable = PostgresBackend.executeQuery(s"SELECT * FROM $tableName;")(rs => rs)
        futureTable
      }
    }
  }

  val tables = Await.result(futureTables, Duration.Inf)

  tables.foreach { table =>
    table.foreach { value =>
      println(value)
    }
  }

  //Thread.sleep(1000)

}
