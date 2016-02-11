package com.fiser.akka.streams.file

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io._
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object LargeFile extends App {
  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()

  val sourceFile = new File("""E:\dev\sample2.csv""")


  val fileSource: Source[ByteString, Future[Long]] = FileIO.fromFile(sourceFile)
    .via(Framing.delimiter(ByteString(System.lineSeparator()), 1048576))
    .map(chunk => chunk.toString())
    .map(_.toUpperCase())
    .map(ByteString(_))

  val sinkFile = new File("""E:\dev\sample_sink.csv""")
  val fileSink: Sink[ByteString, Future[Long]] = FileIO.toFile(sinkFile)


  val result: Future[Long] = fileSource.to(fileSink).run()

  result onComplete {
    case Success(readBytes) =>
      println(s"$readBytes were processed")
    case Failure(ex) => println(ex.getMessage)
  }
}
