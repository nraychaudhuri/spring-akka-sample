package org.springframework.samples.travel.search.messages;

import java.util.List;

import org.springframework.samples.travel.Hotel;

//case class IndexHotels(hotels: Seq[Hotel]) extends SearchMessages

public final class JIndexHotels {
	
	private final List<Hotel> hotels;
	public JIndexHotels(List<Hotel> hotels) {
		this.hotels = hotels;
	}
	
	public List<Hotel> getHotels() {
		return hotels;
	}

}
