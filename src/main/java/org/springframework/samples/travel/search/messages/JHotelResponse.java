package org.springframework.samples.travel.search.messages;

import java.util.List;

import org.springframework.samples.travel.Hotel;

//case class HotelResponse(hotels: Seq[Hotel]) extends SearchMessages

public final class JHotelResponse {

	private final List<Hotel> hotels;
	public JHotelResponse(List<Hotel> hotels) {
		this.hotels = hotels;
	}
	
	public List<Hotel> getHotels() {
		return hotels;
	}
	
}
