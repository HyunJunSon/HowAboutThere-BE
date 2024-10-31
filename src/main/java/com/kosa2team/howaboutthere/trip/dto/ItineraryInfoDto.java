package com.kosa2team.howaboutthere.trip.dto;

import java.util.List;

public record ItineraryInfoDto(String startDate,String endDate, List<SpotInfoDto> touristspots, int budget) {
}
