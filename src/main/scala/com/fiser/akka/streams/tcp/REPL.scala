/*
 * Copyright 2003-2015 Monitise Group Limited. All Rights Reserved.
 *
 * Save to the extent permitted by law, you may not use, copy, modify,
 * distribute or create derivative works of this material or any part
 * of it without the prior written consent of Monitise Group Limited.
 * Any reproduction of this material must contain this notice.
 */
package com.fiser.akka.streams.tcp
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Tcp, Source}
import akka.stream.scaladsl.Tcp.{OutgoingConnection, IncomingConnection, ServerBinding}
import akka.stream.stage.{PushStage, Context, SyncDirective}
import akka.util.ByteString
import akka.stream.io.Framing

import scala.concurrent.Future

object REPL extends App {
  implicit val system = ActorSystem("EchoServer")
  implicit val materializer = ActorMaterializer()

  val connection: Flow[ByteString, ByteString, Future[OutgoingConnection]] = Tcp().outgoingConnection("localhost", 8888)

  val replParser = new PushStage[String, ByteString] {
    override def onPush(elem: String, ctx: Context[ByteString]): SyncDirective = {
      elem match {
        case "q" => ctx.pushAndFinish(ByteString("BYE\n"))
        case _   => ctx.push(ByteString(s"$elem\n"))
      }
    }
  }

  val repl = Flow[ByteString]
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map(text => println("Server: " + text))
    .map(_ => readLine("> "))
    .transform(() => replParser)

  connection.join(repl).run()
}

