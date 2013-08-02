package org.springframework.samples.travel.search.messages;

import org.springframework.samples.travel.SearchCriteria;

public final class JHotelQuery {

	private final SearchCriteria c;
	public JHotelQuery(SearchCriteria search) {
		this.c = search;
	}
	
	public SearchCriteria getCriteria() {
		return c;
	}
}
