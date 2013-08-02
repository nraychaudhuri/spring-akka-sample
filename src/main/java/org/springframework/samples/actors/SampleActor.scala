package org.springframework.samples.actors
import akka.actor.Actor
import akka.event.Logging
 
class SampleActor extends Actor {
  val log = Logging(context.system, this)
  def receive = {
    case "test" => log.info("received test")
    case _       => log.info("received unknown message")
  }
}