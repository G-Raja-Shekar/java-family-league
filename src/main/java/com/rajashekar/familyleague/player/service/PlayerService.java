package com.rajashekar.familyleague.player.service;

import com.rajashekar.familyleague.player.dto.CreatePlayerRequest;
import com.rajashekar.familyleague.player.dto.PlayerResponse;
import com.rajashekar.familyleague.player.dto.PlayerSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlayerService {

    PlayerResponse create(CreatePlayerRequest request);

    PlayerResponse getById(Long id);

    Page<PlayerSummaryResponse> list(Long teamId, String search, Pageable pageable);

    void softDelete(Long id);
}
