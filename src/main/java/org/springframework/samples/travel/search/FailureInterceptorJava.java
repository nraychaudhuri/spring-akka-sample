package org.springframework.samples.travel.search;

import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.*;
import org.springframework.samples.travel.search.messages.JHotelResponse;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;

public class FailureInterceptorJava extends UntypedActor {

	private ActorRef detector;
	private ActorRef listener;

	public FailureInterceptorJava(ActorRef detector, ActorRef listener) {
		this.detector = detector;
		this.listener = listener;	
		getContext().setReceiveTimeout(Duration.create("1 seconds"));
	}
	
	
	@Override
	public void onReceive(Object msg) throws Exception {
		if(msg instanceof Exception) {
			detector.tell(new JQueryFail(), getSelf());
			listener.tell(msg, getSelf());
			getContext().stop(getSelf());
		}
		
		if(msg instanceof JHotelResponse) {
			listener.tell(msg, getSelf());
			detector.tell(new JQueryOk(), getSelf());
		}
		
		if(msg instanceof ReceiveTimeout) {
			listener.tell(new FailWhaleException("Search Service is unavailable"), null);
			detector.tell(new JQueryFail(), null);
			getContext().stop(getSelf());
		}
	
	}

}
