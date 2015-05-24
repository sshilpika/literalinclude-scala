package luc.literalinclude.service

/**
 * Created by sshilpika on 5/13/15.
 */

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._
import scala.util.Properties

object Boot extends App {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("literalinclude")

  // create and start our service actor
  val service = system.actorOf(Props[MyServiceActor], "literalinclude-service")

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  val port = Properties.envOrElse("PORT", "8080").toInt
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = port)

}
