/*
 * Copyright 2003-2015 Monitise Group Limited. All Rights Reserved.
 *
 * Save to the extent permitted by law, you may not use, copy, modify,
 * distribute or create derivative works of this material or any part
 * of it without the prior written consent of Monitise Group Limited.
 * Any reproduction of this material must contain this notice.
 */
package com.fiser.akka.streams.tcp
import scala.concurrent.ExecutionContext.Implicits.global

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Sink, Source, Tcp}

import scala.concurrent.Future
import scala.util.Success

object ReverseProxy extends App {
  implicit val system = ActorSystem("on-to-one-proxy")
  implicit val materializer = ActorMaterializer()

  val connections: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind("localhost", 6000)

  val sink = Sink.foreach[Tcp.IncomingConnection] {
    connection =>
      println(s"Client connected from: ${connection.remoteAddress}")
      connection.handleWith(Tcp().outgoingConnection(new InetSocketAddress("localhost", 7000)))
  }
  val materializedServer: Future[ServerBinding] = connections.to(sink).run()
  materializedServer.onComplete{
    case Success(binding) =>
      println(binding)
  }
}
