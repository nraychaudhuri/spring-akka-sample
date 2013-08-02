package org.springframework.samples.travel.search;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.samples.travel.search.messages.*;
import org.springframework.samples.travel.Hotel;
import org.springframework.samples.travel.SearchCriteria;

import scala.Option;

import akka.actor.UntypedActor;

public class SingleActorSearch extends UntypedActor {

	private Map<String, Hotel> index;
	
	int counter = 0;
	
	public SingleActorSearch(List<Hotel> hotels) {
		this.index = indexHotels(hotels);
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
	   if(message instanceof JHotelQuery) {		   
		 JHotelQuery q = (JHotelQuery)message;
		 if(q.getCriteria().getSearchString().equals("timeout")) { /*do nothing*/ }
		 else if(q.getCriteria().getSearchString().equals("fail")) { 
			 throw new RuntimeException("O NOES - FAKE FAILUREZ"); 
		 }
		 else { getSender().tell(findHotels(q.getCriteria()), getSelf()); }
	   }
	}
	
	@Override
	public void preRestart(Throwable reason, Option<Object> msg) {
	   System.out.println("[][][] restarting after exception on message: " + msg + " [][][]");

	}

	private JHotelResponse findHotels(SearchCriteria criteria) {

		List<Hotel> matchedHotels = new ArrayList<Hotel>();
		for (Entry<String, Hotel> entry : index.entrySet()) {
			if(entry.getKey().contains(criteria.getSearchString())){
				matchedHotels.add(entry.getValue());
			}
		}
		return new JHotelResponse(matchedHotels);
	}

	private Map<String, Hotel> indexHotels(List<Hotel> hotels) {
		Map<String, Hotel> indexedHotels = new HashMap<String, Hotel>();
		for (Hotel hotel : hotels) {
			indexedHotels.put(makeSearchableString(hotel), hotel);
		}
		return indexedHotels;
	}
	
	private String makeSearchableString(Hotel h) {
		return (h.getAddress() + " " + h.getName() + " " + h.getCity() + " " + h.getState() + " " + h.getZip()).toLowerCase();
	}
	
}