package com.sparta.company.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetHubInfoRes {
    private UUID hubId;
    private String hubName;
    private String address;
    private double lati;
    private double longti;
    @JsonProperty("centerHub")
    private boolean isCenterHub;
}