package com.kosa2team.howaboutthere.trip.controller;

import com.kosa2team.howaboutthere.trip.dto.*;
import com.kosa2team.howaboutthere.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TripController {
    private final TripService tripService;

    @GetMapping()
    public String health(){
        return "OK";
    }

    @PostMapping("/theme")
    public ResponseEntity<List<ThemeDto>> generateThema(@RequestBody TripInfoDto dto){
        return ResponseEntity.ok(tripService.generateThema(dto));
    }

    @PostMapping("/spot")
    public ResponseEntity<List<SpotInfoDto>> generateSpot(@RequestBody ThemeDto dto){
        return ResponseEntity.ok(tripService.generateSpot(dto));
    }

    @PostMapping("/itinerary")
    public ResponseEntity<List<ItineraryDto>> generateItinerary(@RequestBody ItineraryInfoDto dto){
        return ResponseEntity.ok(tripService.generateItinerary(dto));
    }

}
