package com.fiser.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent.ExecutionContext.Implicits.global

object StreamProgram extends App {

  val sayFlow: Flow[String, String, Unit] =
    Flow[String].map { s =>
      s + "."
    }

  val shoutFlow: Flow[String, String, Unit] =
    Flow[String].map { s =>
      s + "!!!!"
    }

  val sayAndShoutFlow: Flow[String, String, Unit] =
    Flow() { implicit b =>
      import FlowGraph.Implicits._

      val broadcast = b.add(Broadcast[String](2))
      val merge = b.add(Merge[String](2))

      broadcast ~> sayFlow ~> merge
      broadcast ~> shoutFlow ~> merge
      (broadcast.in, merge.out)
    }

  def run(): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    Source(List("Hello World"))
      .via(sayAndShoutFlow)
      .runWith(Sink.foreach(println))
      .onComplete {
        case _ => system.shutdown()
      }
  }

  run()
}