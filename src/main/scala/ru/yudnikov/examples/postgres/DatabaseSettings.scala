package ru.yudnikov.examples.postgres

import java.sql.{Connection, DriverManager}

import org.postgresql.PGConnection

case class DatabaseSettings(host: String, port: Int, database: String, login: String, password: String) {
  def postgresConnection: Connection with PGConnection = {
    DriverManager.registerDriver(new org.postgresql.Driver)
    DriverManager.getConnection(s"jdbc:postgresql://$host:$port/$database", login, password).asInstanceOf[Connection with PGConnection]
  }
}