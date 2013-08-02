package org.springframework.samples.travel
package search

import akka.actor.{Actor, ActorRef, Props}
import org.springframework.samples.travel.search.messages.JHotelResponse
import org.springframework.samples.travel.search.messages.JHotelQuery

/** This message is passed from the query interceptor -> query cache. */
case class UpdateQueryCache(query: String, response: JHotelResponse)


/** This actor can put a query cache in the front of a query service. */
class QueryCacheActor(service: ActorRef, cachedQueries: Int) extends Actor {
  val cache = new LruCache[String, JHotelResponse](cachedQueries)
  def receive: Receive = {
    case hq: JHotelQuery =>
      val queryString = hq.getCriteria.getSearchString.toLowerCase
      Option(cache get queryString) match {
        // query is not cached.
        case None =>
          // Create actor to handle response.
          val listener = sender
          val interceptor = context.actorOf(Props(new QueryCacheInterceptor(queryString, listener, context.self)))
          //Send the query down to the service *as if it came from* the interceptor.
          service.tell(hq, interceptor)
        // Query is cached.
        case Some(cached) =>
           println("[][][] RETURNING CACHED: " + queryString + " [][][]")
           sender ! cached
      }
    case UpdateQueryCache(query, response) =>
      cache.put(query, response)
  }
}


/** This class intercepts query results and also sends them to the query cache. */
class QueryCacheInterceptor(query: String, listener: ActorRef, cache: ActorRef) extends Actor {
  def receive: Receive = {
    case ex: Exception =>
      // Feed failure on, don't keep them to yourself!
      listener ! ex
    case response: JHotelResponse =>
      listener ! response
      cache ! UpdateQueryCache(query, response)
      context stop self
  }
}

/** An LRU cache using a linked hash map. */
class LruCache[A,B](maxEntries: Int) extends java.util.LinkedHashMap[A, B](maxEntries + 1, 1.0f, true) {
  override protected def removeEldestEntry(eldest: java.util.Map.Entry[A, B]): Boolean = 
    super.size() > maxEntries
}

