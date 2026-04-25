package com.rajashekar.familyleague.season.service;

import com.rajashekar.familyleague.season.dto.CreateSeasonRequest;
import com.rajashekar.familyleague.season.dto.SeasonResponse;
import com.rajashekar.familyleague.season.dto.SeasonSummaryResponse;
import com.rajashekar.familyleague.season.entity.SeasonStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SeasonService {

    SeasonResponse create(CreateSeasonRequest request);

    SeasonResponse getById(Long id);

    Page<SeasonSummaryResponse> list(Pageable pageable);

    Page<SeasonSummaryResponse> listByLeague(Long leagueId, Pageable pageable);

    Page<SeasonSummaryResponse> listByStatus(SeasonStatus status, Pageable pageable);

    SeasonResponse updateStatus(Long id, SeasonStatus newStatus);

    void softDelete(Long id);
}
