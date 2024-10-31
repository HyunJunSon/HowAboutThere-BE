package com.kosa2team.howaboutthere.trip.dto;

import java.util.List;

public record ItineraryDto(String day, String summary,List<SpotDTO> spots) {
}
