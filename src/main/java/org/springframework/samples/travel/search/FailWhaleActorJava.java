package org.springframework.samples.travel.search;

import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.FailWhaleException;
import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.JBecomeBad;
import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.JBecomeOk;
import org.springframework.samples.travel.search.messages.JHotelQuery;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.japi.Procedure;

public class FailWhaleActorJava extends UntypedActor{
	
	private ActorRef service;
	public FailWhaleActorJava(ActorRef service) {
		this.service = service;
	}

	private ActorRef failDetector = getContext().actorOf(new Props(new UntypedActorFactory() {
		public UntypedActor create() {
		   return new FailDetectorJava(getSelf());
		}
	}), "failure-detector");
	
	@Override
	public void onReceive(Object msg) throws Exception {
		passing.apply(msg);
	}
	
	
	private Procedure<Object> passing = new Procedure<Object>() {
	    @Override
	    public void apply(Object msg) {
	    	if(msg instanceof JHotelQuery) {
	    		final ActorRef listener = getSender();
	    		ActorRef interceptor = getContext().actorOf(new Props(new UntypedActorFactory() {
	    			public UntypedActor create() {
	    				   return new FailureInterceptorJava(failDetector, listener);
	    				}
	    			}));
	    		
	    		service.tell(msg, interceptor);
	    		
	    	}
	    	if(msg instanceof JBecomeBad) {
	    	    System.out.println("[][][] Fail Whale -> Becoming BAAAAAAAAAAAD [][][]");
	    		getContext().become(failing);
	    	}
	    }
	};

	private Procedure<Object> failing = new Procedure<Object>() {
	    @Override
	    public void apply(Object msg) {
	    	if(msg instanceof JHotelQuery) {
	    		getSender().tell(new FailWhaleException("Search Service is unavailable"), getSelf());
	    		
	    	}
	    	if(msg instanceof JBecomeOk) {
	    	    System.out.println("[][][] Fail Whale -> Becoming OK [][][]");
	    		getContext().become(passing);
	    	}
	    }
	};

}


