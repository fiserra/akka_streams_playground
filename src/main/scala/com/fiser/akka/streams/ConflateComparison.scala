package com.fiser.akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import scala.concurrent.ExecutionContext.Implicits.global

object ConflateComparison {

  val fastStage: Flow[Unit, Unit, Unit] =
    Flow[Unit].map(_ => println("this is the fast stage"))

  val slowStage: Flow[Unit, Unit, Unit] =
    Flow[Unit].map { _ =>
      Thread.sleep(1000L)
      println("this is the SLOWWWW stage")
    }

  val conflateFlow: Flow[Unit, Unit, Unit] =
    Flow[Unit].conflate(_ => List(()))((l, u) => u :: l)
      .mapConcat(identity)

  val withConflate: Source[Unit, Unit] =
    Source(List.fill(10)(()))
      .via(fastStage)
      .via(conflateFlow)
      .via(slowStage)

  val withoutConflate: Source[Unit, Unit] =
    Source(List.fill(10)(()))
      .via(fastStage)
      .via(slowStage)

  def run(s: Source[Unit, Unit]): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    s.runWith(Sink.ignore)
      .onComplete { _ => system.shutdown() }
  }
}
