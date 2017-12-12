package ru.yudnikov.superkassa

object Messages {
  case object RegisterBackend
  case object UnregisterBackend
  case object JobRequest
  case class JobRequest(count: Long)
}
