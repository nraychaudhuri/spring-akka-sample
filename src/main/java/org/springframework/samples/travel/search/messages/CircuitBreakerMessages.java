package org.springframework.samples.travel.search.messages;

public final class CircuitBreakerMessages {

	public static final class JQueryOk {}
	public static final class JQueryFail {}
	public static final class JBecomeOk {}
	public static final class JBecomeBad {}
	
	public static class FailWhaleException extends Exception {
		public FailWhaleException(String msg) {
			super(msg);
		}
	}
	/** This message is passed from the query interceptor -> query cache. */
//	case object QueryOk
//	case object QueryFail
//	case object BecomeOk
//	case object BecomeBad

}
