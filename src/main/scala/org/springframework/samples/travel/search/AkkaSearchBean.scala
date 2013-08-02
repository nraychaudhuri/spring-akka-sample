package org.springframework.samples.travel
package search

import org.springframework.stereotype.Service
import javax.inject.{Inject,Singleton}
import javax.persistence.{EntityManager, PersistenceContext}
import akka.actor._
import scala.collection.JavaConverters._
import javax.annotation.PostConstruct
import akka.pattern.ask
import scala.concurrent.Await
import javax.annotation.PreDestroy
import akka.routing.RoundRobinRouter
import collection.JavaConverters._
import scala.concurrent.duration._
import akka.util.Timeout
import org.springframework.samples.travel.search.messages.JHotelQuery
import org.springframework.samples.travel.search.messages.JHotelResponse

@Service
@Singleton
class AkkaSearchBean extends SearchService  {
  /** The entity manager for this bean. */
  @(PersistenceContext @annotation.target.setter)
  var em: EntityManager = null
  
  /** The ActorSystem that runs under this bean. */
  val system =  ActorSystem("search-service")
  
  /** Returns the current search service front-end actor. */
  def searchActor: ActorRef = system actorFor (system / "search-service-frontend")
  
  @PostConstruct
  def makeSearchActor = {
    // Startup....
    def getHotels = {
      val hotels = em.createQuery("select h from Hotel h").getResultList.asInstanceOf[java.util.List[Hotel]].asScala
      hotels foreach em.detach
      hotels
    }
    
    val singleActorSearchProps = new Props(new UntypedActorFactory() {
  	  def create(): UntypedActor = {
  	    return new SingleActorSearch(getHotels.asJava);
  	  }
    });
    //system.actorOf(singleActorSearchProps, "search-service-frontend");
    // Now feed data into Akka Search service.
//   val router = RoundRobinRouter(nrOfInstances=5)    
//   val rawService = system.actorOf(singleActorSearchProps withRouter router, "search-service-frontend")
    
     val searchTreeProps = Props(new CountryCategoryActor(getHotels))
     val rawService = system.actorOf(searchTreeProps, "search-tree")
   
     val failWhaleProps = new Props(new UntypedActorFactory() {
	   def create(): UntypedActor = {
	     return new FailWhaleActorJava(rawService);
	   }
     })
     val failWhale = system.actorOf(failWhaleProps, "auto-timeout-handler")
     val cacheProps = Props(new QueryCacheActor(failWhale, 5))
     val cached = system.actorOf(cacheProps, "search-service-frontend")
    ()
  }
  
  @PreDestroy
  def shutdown(): Unit = system.shutdown()
  
  override def findHotels(criteria: SearchCriteria): java.util.List[Hotel] = {
    implicit val timeout = Timeout(5, SECONDS)
    val response = (searchActor ? new JHotelQuery(criteria))
    Await.result(response, Duration.Inf) match {
      case response: JHotelResponse => response.getHotels()
      case ex: Exception => throw ex
    }
  }
}

