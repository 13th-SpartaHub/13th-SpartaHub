package com.sparta.ai.domain.controller;

import com.sparta.ai.domain.dto.request.AiMessageRequestDto;
import com.sparta.ai.domain.dto.response.AiMessageCreateResponseDto;
import com.sparta.ai.domain.dto.response.AiMessageIdResponseDto;
import com.sparta.ai.domain.dto.response.AiMessageResponseDto;
import com.sparta.ai.domain.service.AiService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/ai-messages")
public class AiController {

    private final AiService aiService;

    @PostMapping
    public ResponseEntity<AiMessageCreateResponseDto> createAiMessage(
            @RequestBody AiMessageRequestDto requestDto,
            @RequestHeader("X-User-Username") String requestUsername,
            @RequestHeader("X-User-Role") String requestRole) {

        AiMessageCreateResponseDto responseDto = aiService.createAiMessage(requestDto, requestUsername, requestRole);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping("{aiMessageId}")
    public ResponseEntity<AiMessageResponseDto> getAiMessage(
            @PathVariable UUID aiMessageId,
            @RequestHeader("X-User-Role") String requestRole) {

        AiMessageResponseDto responseDto = aiService.getAiMessage(aiMessageId, requestRole);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<PagedModel<AiMessageResponseDto>> searchAiMessages(
            @RequestParam(required = false) String username,
            @RequestHeader("X-User-Role") String requestRole,
            @PageableDefault(sort = "createdAt") Pageable pageable) {

        Page<AiMessageResponseDto> responseDtos = aiService.searchAiMessages(username, requestRole, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(responseDtos));
    }

    @DeleteMapping("{aiMessageId}")
    public ResponseEntity<AiMessageIdResponseDto> deleteAiMessage(
            @PathVariable UUID aiMessageId,
            @RequestHeader("X-User-Username") String requestUsername,
            @RequestHeader("X-User-Role") String requestRole) {

        AiMessageIdResponseDto responseDto = aiService.deleteAiMessage(aiMessageId, requestUsername, requestRole);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}
