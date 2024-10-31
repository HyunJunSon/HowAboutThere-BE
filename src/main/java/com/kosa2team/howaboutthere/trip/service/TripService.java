package com.kosa2team.howaboutthere.trip.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlacesSearchResult;
import com.kosa2team.howaboutthere.trip.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TripService {

    private final GeoApiContext geoApiContext;
    private final ChatClient chatClient;

    @Value("classpath:/templates/themePrompt.st")
    private Resource themaPrompt;

    @Value("classpath:/templates/touristSpotPrompt.st")
    private Resource spotPrompt;

    @Value("classpath:/templates/ItineraryPrompt.st")
    private Resource itineraryPrompt;

    /**
     * thema 생성
     * @param dto : TripInfoDto로 startDate, endDate, budget, isDomestic 받아옴
     * @return List<ThemaDto> : 생성된 thema 리스트 반환
     */
    public List<ThemeDto> generateThema(TripInfoDto dto) {
        // dto to map
        ObjectMapper object = new ObjectMapper();
        Map<String,Object> map = object.convertValue(dto,Map.class);
        // promptTemplate 호출 후 데이터 삽입
        PromptTemplate pt = new PromptTemplate(themaPrompt);
        Prompt prompt = pt.create(map);
        //bedrock client 호출 후 ThemaDto에 넣어 리스트로 반환
        return chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<List<ThemeDto>>() {});
    }

    /**
     * tourist spot 생성
     * @param dto : ThemaDto로 region, thema 받아옴
     * @return String : 생성된 tourist spot 반환
     */
    public List<SpotInfoDto> generateSpot(ThemeDto dto) {

        ObjectMapper object = new ObjectMapper();
        Map<String, Object> map = object.convertValue(dto,new TypeReference<>() {});
        PromptTemplate pt = new PromptTemplate(spotPrompt);
        Prompt prompt = pt.create(map);
        List<TouristSpotDto> response = chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<List<TouristSpotDto>>() {});
        return verifyPlace(response);

    }

    public List<SpotInfoDto> verifyPlace(List<TouristSpotDto> touristspots) {
        List<SpotInfoDto> spots = new ArrayList<>();
        for (TouristSpotDto spotsList : touristspots) {
            spotsList.spotname();
            try {
                PlacesSearchResult[] results = PlacesApi.textSearchQuery(geoApiContext, spotsList.spotname()).language("ko").await().results;
                if (results.length > 0) {
                    PlacesSearchResult placeDetails = results[0];
                    spots.add(new SpotInfoDto(placeDetails.placeId,placeDetails.name,placeDetails.formattedAddress));
                }
            } catch (IOException | ApiException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return spots;
    }

    public List<ItineraryDto> generateItinerary(ItineraryInfoDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("startDate", dto.startDate());
        map.put("endDate", dto.endDate());
        map.put("budget", dto.budget());
        List<String> spots = new ArrayList<>();
        for (SpotInfoDto spotsList : dto.touristspots()) {
            spots.add(spotsList.spotname());
        }
        String spotList = String.join(",", spots);
        map.put("touristspots", spotList);
        PromptTemplate pt = new PromptTemplate(itineraryPrompt);
        Prompt prompt = pt.create(map);
        return chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<List<ItineraryDto>>() {});
    }



}
