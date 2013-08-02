package org.springframework.samples.travel
package search

import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.SECONDS
import org.springframework.samples.travel.Hotel
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ReceiveTimeout
import akka.actor.actorRef2Scala
import org.springframework.samples.travel.search.messages.JHotelQuery
import org.springframework.samples.travel.search.messages.JHotelResponse
import scala.collection.JavaConverters._

/** The "head" actor for splitting data by country.
 * 
 * Sub-categories are also "heads" by city.
 */
class CountryCategoryActor(hotels: Seq[Hotel]) extends CategoryActor(hotels) {
  protected def category(hotel: Hotel): String = 
    Option(hotel) map(_.getCountry) getOrElse ""
  protected def newChild(hotels: Seq[Hotel]): Actor =
    new CityCategoryActor(hotels)
}
/**
 * The "head" actor for splitting data by city.
 * 
 * Sub-categories are full indexes by city.
 */
class CityCategoryActor(hotels: Seq[Hotel]) extends CategoryActor(hotels) {
  protected def category(hotel: Hotel): String = 
    Option(hotel.getCity) getOrElse ""
  protected def newChild(hotels: Seq[Hotel]): Actor =
    new SingleActorSearch(hotels.asJava)
}

/** This represents a category in the scatter gather model.
 * This class can be extended to define whether it has topic nodes or
 * other categories.
 * 
 * The `category` and `newChild` methods are overridable to generate
 * specific hierarchy members of this tree.
 */
abstract class CategoryActor(hotels: Seq[Hotel]) extends Actor {
  val children = for {
    (cat, hotels) <- hotels groupBy category
    actorProps = Props(newChild(hotels))
  } yield context.actorOf(actorProps, scrubActorName(cat))
  
  
  def receive: Receive = {
    case query: JHotelQuery =>
      // Create interceptor.
      val listener = sender
      val gathererProps = Props(new Gatherer(listener, children.size))
      val gatherer = context.actorOf(gathererProps)
      // Scatter to our children
      for(child <- children) {
        child.tell(query, gatherer)
      }
  }
  
  private def scrubActorName(name: String) =
    name.replaceAll("[^-\\w:@&=+,.!~*'$_;]", "-")
  
  // TODO - Configurable and mutliple splits
  protected def category(hotel: Hotel): String
  protected def newChild(hotels: Seq[Hotel]): Actor
}



/** This actor is responsible for listening to responses from topic nodes
 * and gathering all the results before forwarding on.
 */
class Gatherer(listener: ActorRef, numNodes: Long) extends Actor {
  var responses: Seq[JHotelResponse] = Seq.empty
    
  def receive: Receive = {
    case ex: Exception =>
      listener ! ex
    case response: JHotelResponse =>
      responses = responses :+ response
      if(responses.size >= numNodes) joinResponses()
      else context setReceiveTimeout (1 seconds)
      
    case ReceiveTimeout =>
      listener ! new Exception("GATHERER - Query did not return!")
  }
  
  def joinResponses():  Unit = {
    // TODO - Sort appropriately.
    listener ! new JHotelResponse((responses flatMap (_.getHotels().asScala)).asJava)
    // We're done as an actor now.
    context stop self
  }
  
  
  context setReceiveTimeout (Duration(1, SECONDS))
}