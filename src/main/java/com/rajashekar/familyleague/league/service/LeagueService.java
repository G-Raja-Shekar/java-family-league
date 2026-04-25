package com.rajashekar.familyleague.league.service;

import com.rajashekar.familyleague.league.dto.CreateLeagueRequest;
import com.rajashekar.familyleague.league.dto.LeagueResponse;
import com.rajashekar.familyleague.league.dto.LeagueSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeagueService {

    LeagueResponse create(CreateLeagueRequest request);

    LeagueResponse getById(Long id);

    Page<LeagueSummaryResponse> list(String search, Pageable pageable);

    void softDelete(Long id);
}
