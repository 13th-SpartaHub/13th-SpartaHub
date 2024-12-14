package com.sparta.shipment.domain.controller;

import com.sparta.shipment.domain.dto.request.CreateShipmentRequestDto;
import com.sparta.shipment.domain.dto.request.UpdateShipmentRequestDto;
import com.sparta.shipment.domain.dto.response.ShipmentResponseDto;
import com.sparta.shipment.domain.service.ShipmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // 배송 추가 API
    @PostMapping
    public ResponseEntity<ShipmentResponseDto> createShipment(
            @Valid @RequestBody CreateShipmentRequestDto request,
            @RequestHeader("X-User-Username") String requestUsername,
            @RequestHeader("X-User-Role") String requestRole) {
        ShipmentResponseDto response = shipmentService.createShipment(request, requestUsername, requestRole);
        return ResponseEntity.status(HttpStatus.CREATED).body((response));
    }

    // 배송 삭제 API
    @DeleteMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponseDto> deleteShipment(
            @PathVariable UUID shipmentId,
            @RequestHeader("X-User-Username") String requestUsername,
            @RequestHeader("X-User-Role") String requestRole) {
        ShipmentResponseDto response = shipmentService.deleteShipment(shipmentId,
                requestUsername, requestRole);
        return ResponseEntity.status(HttpStatus.OK).body((response));
    }

    //배송 수정 API
    @PatchMapping("/{shipmentId}")
    public ResponseEntity<ShipmentResponseDto> updateShipment(@PathVariable UUID shipmentId,
                                                              @RequestBody UpdateShipmentRequestDto request,
                                                              @RequestHeader("X-User-Username") String requestUsername,
                                                              @RequestHeader("X-User-Role") String requestRole) {
        ShipmentResponseDto response = shipmentService.updateShipment(shipmentId, request,
                requestUsername, requestRole);
        return ResponseEntity.status(HttpStatus.OK).body((response));
    }
}