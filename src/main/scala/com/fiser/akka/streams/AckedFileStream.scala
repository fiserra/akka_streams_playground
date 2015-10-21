/*
 * Copyright 2003-2015 Monitise Group Limited. All Rights Reserved.
 *
 * Save to the extent permitted by law, you may not use, copy, modify,
 * distribute or create derivative works of this material or any part
 * of it without the prior written consent of Monitise Group Limited.
 * Any reproduction of this material must contain this notice.
 */
package com.fiser.akka.streams

import java.io.ByteArrayInputStream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.io._
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AckedFileStream {
  class QueueMessage(str: String) {
    def ack(): Unit = println("acked")
    lazy val stream = new ByteArrayInputStream(str.getBytes)
  }

  sealed trait Element
  case class StringElement(s: String) extends Element
  case class QueueElement(qm: QueueMessage) extends Element

  def fileStream(m: QueueMessage): Source[StringElement, Future[Long]] =
    InputStreamSource(() => m.stream)
      .via(Framing.delimiter(
        ByteString("\n"), maximumFrameLength = Int.MaxValue,
        allowTruncation = true))
      .map(s => StringElement(s.utf8String))

  def run(ls: List[String]): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    Source(ls.map(s => new QueueMessage(s)))
      .map { qm =>
        fileStream(qm).concat(Source(List(QueueElement(qm))))
      }.flatten(FlattenStrategy.concat)
      .runWith(Sink.foreach { (r: Element) => r match {
        case QueueElement(qm) => qm.ack()
        case StringElement(s) => println(s)
      }})
      .onComplete(_ => system.shutdown())
  }
}
