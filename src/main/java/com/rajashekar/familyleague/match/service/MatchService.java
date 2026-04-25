package com.rajashekar.familyleague.match.service;

import com.rajashekar.familyleague.match.dto.CreateMatchRequest;
import com.rajashekar.familyleague.match.dto.MatchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatchService {

    MatchResponse create(Long seasonId, CreateMatchRequest request);

    MatchResponse getById(Long id);

    Page<MatchResponse> listBySeason(Long seasonId, Pageable pageable);

    void softDelete(Long id);
}
