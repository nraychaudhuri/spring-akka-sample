package org.springframework.samples.travel.search;

import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.JBecomeBad;
import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.JBecomeOk;
import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.JQueryFail;
import org.springframework.samples.travel.search.messages.CircuitBreakerMessages.JQueryOk;

import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import akka.actor.UntypedActor;

public class FailDetectorJava extends UntypedActor {

	private int badQueryCount = 0;
	private ActorRef whale;
	public FailDetectorJava(ActorRef whale) {
		this.whale = whale;		
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		if(msg instanceof JQueryOk) {
			if(badQueryCount > 0) { badQueryCount -= 1; }
		}
		if(msg instanceof JQueryFail) {
			badQueryCount += 1;
			if(badQueryCount > 3) {
				// Over our limit.   Flip to fail whale and wait 30 seconds.
				whale.tell(new JBecomeBad(), getSelf());
				getContext().setReceiveTimeout(
						Duration.apply(30L, java.util.concurrent.TimeUnit.SECONDS));
				badQueryCount = 0;
			}
		}
		if(msg instanceof ReceiveTimeout) {
		    // We waited long enough, flip the whale.
			whale.tell(new JBecomeOk(), getSelf());
		}
	}

}
