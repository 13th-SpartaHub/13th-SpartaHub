package com.sparta.Hub.domain.service;

import com.sparta.Hub.domain.dto.request.CreateHubRouteReq;
import com.sparta.Hub.domain.dto.response.CreateHubRouteRes;
import com.sparta.Hub.domain.dto.response.GetHubRouteInfoRes;
import com.sparta.Hub.domain.dto.response.KakaoApiRes;
import com.sparta.Hub.exception.HubExceptionMessage;
import com.sparta.Hub.exception.HubRouteExceptionMessage;
import com.sparta.Hub.model.entity.Hub;
import com.sparta.Hub.model.entity.HubRoute;
import com.sparta.Hub.model.repository.HubRepository;
import com.sparta.Hub.model.repository.HubRouteRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class HubRouteService {

  @Value("${kakao.api.key}")
  private String apiKey;

  private final HubRouteRepository hubRouteRepository;
  private final HubRepository hubRepository;

  public CreateHubRouteRes createHubRoute(
      CreateHubRouteReq createHubRouteReq,
      String requestUsername,
      String requestRole
  ) {
    //validateRole(requestRole);
    validateEquealHub(createHubRouteReq);

    Hub startHub = validateHub(createHubRouteReq.getStratHubId());
    Hub endHub = validateHub(createHubRouteReq.getEndHubId());


    KakaoApiRes kakaoApiRes = KakaoMapApi(startHub,endHub);
    HubRoute hubRoute = HubRoute.builder()
        .startHub(startHub)
        .endHub(endHub)
        .deliveryTime(kakaoApiRes.getDeliveryTime())
        .distance(kakaoApiRes.getDistance())
        .build();

    hubRouteRepository.save(hubRoute);

    return CreateHubRouteRes.builder()
        .hubRouteId(hubRoute.getHubId())
        .deliveryTime(hubRoute.getDeliveryTime())
        .distance(hubRoute.getDistance())
        .build();
  }

  @Cacheable(cacheNames = "hubroutecache",key = "args[0]")
  public GetHubRouteInfoRes getHubRoute(UUID hubRouteId) {
    HubRoute hubRoute = validateExistHubRoute(hubRouteId);
    return GetHubRouteInfoRes.builder()
        .hubRouteId(hubRoute.getHubId())
        .distance(hubRoute.getDistance())
        .deliveryTime(hubRoute.getDeliveryTime())
        .build();

  }
  private void validateRole(String requestRole) {
    if(!requestRole.equals("MASTER")){
      throw new IllegalArgumentException(HubExceptionMessage.NOT_ALLOWED_API.getMessage());
    }
  }
  private HubRoute validateExistHubRoute(UUID hubRouteId) {
    return hubRouteRepository.findById(hubRouteId).orElseThrow(()->
        new IllegalArgumentException(HubRouteExceptionMessage.HUB_ROUTE_NOT_EXIST.getMessage()));
  }

  private void validateEquealHub(CreateHubRouteReq createHubRouteReq) {
    if(createHubRouteReq.getStratHubId().equals(createHubRouteReq.getEndHubId())){
      throw new IllegalArgumentException(HubRouteExceptionMessage.HUB_ROUTE_EQUEAL.getMessage());
    }
  }

  private Hub validateHub(UUID hubId) {
    return hubRepository.findById(hubId).orElseThrow(() -> new IllegalArgumentException(
        HubExceptionMessage.HUB_NOT_EXIST.getMessage()));
  }
  private KakaoApiRes KakaoMapApi(Hub startHub, Hub endHub) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "KakaoAK " + apiKey);

    HttpEntity<String> entity = new HttpEntity<>("", headers);

    String url = "https://apis-navi.kakaomobility.com/v1/directions?" +
        "origin=" + startHub.getLongti() + "," + startHub.getLati() +
        "&destination=" + endHub.getLongti() + "," + endHub.getLati();

    RestTemplate restTemplate = new RestTemplate();
    // ResponseEntity<Map>에서 JSON 파싱
    ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);


    List<Map<String, Object>> routes = (List<Map<String, Object>>) response.getBody().get("routes");
    Map<String, Object> route = routes.get(0);  // 첫 번째 경로

    Map<String, Object> summary = (Map<String, Object>) route.get("summary");

    Object distance = summary.get("distance");
    Object duration = summary.get("duration");
    Integer meter =(Integer) distance;
    Integer second = (Integer) duration;

    LocalDateTime deliveryTime = LocalDateTime.now().plus(Duration.ofSeconds(second));

    BigDecimal meterBD= new BigDecimal(meter);
    BigDecimal kilometerBD = meterBD.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);

    // 응답 본문에서 필요한 필드만 추출
    return KakaoApiRes.builder()
        .deliveryTime(deliveryTime)
        .distance(kilometerBD)
        .build();
  }


}