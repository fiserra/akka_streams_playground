package com.fiser.akka.streams.file


import akka.actor.ActorSystem
import akka.stream.scaladsl.{Source, Sink}
import akka.stream.ActorMaterializer

import java.io.File

object FileSource extends App {
  implicit val sys = ActorSystem()
  implicit val mat = ActorMaterializer()

  val f = scala.io.Source.fromFile(new File("/home/rfiser/.bashrc"))
  Source.fromIterator(() => f.iter)
    .map(c => { print(c); c })
    .runWith(Sink.onComplete(_ ⇒ { f.close(); sys.shutdown() } ))
}
