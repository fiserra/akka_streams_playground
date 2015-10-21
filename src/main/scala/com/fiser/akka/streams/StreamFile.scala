/*
 * Copyright 2003-2015 Monitise Group Limited. All Rights Reserved.
 *
 * Save to the extent permitted by law, you may not use, copy, modify,
 * distribute or create derivative works of this material or any part
 * of it without the prior written consent of Monitise Group Limited.
 * Any reproduction of this material must contain this notice.
 */
package com.fiser.akka.streams
import akka.actor.ActorSystem
import akka.util.ByteString
import akka.stream.{ActorMaterializer, ActorAttributes}
import akka.stream.io._
import java.io.{InputStream, ByteArrayInputStream}
import akka.stream.scaladsl._
import scala.concurrent.ExecutionContext.Implicits.global

object StreamFile {

  def run(): Unit = {
    implicit lazy val system = ActorSystem("example")
    implicit val materializer = ActorMaterializer()
    val is = new ByteArrayInputStream("hello\nworld".getBytes)
    InputStreamSource(() => is)
      .withAttributes(ActorAttributes.dispatcher("akka.stream.default-file-io-dispatcher"))
      .via(Framing.delimiter(
        ByteString("\n"), maximumFrameLength = Int.MaxValue,
        allowTruncation = true))
      .map(_.utf8String)
      .runWith(Sink.foreach(println))
      .onComplete(_ => system.shutdown())
  }
}
