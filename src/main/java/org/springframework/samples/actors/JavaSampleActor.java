package org.springframework.samples.actors;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class JavaSampleActor extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	public void onReceive(Object message) throws Exception {
		if (message.toString() == "test")
			log.info("received test");
		else
			log.info("received unknown message");
	}
}
