package com.rajashekar.familyleague.team.service;

import com.rajashekar.familyleague.team.dto.CreateTeamRequest;
import com.rajashekar.familyleague.team.dto.TeamResponse;
import com.rajashekar.familyleague.team.dto.TeamSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {

    TeamResponse create(CreateTeamRequest request);

    TeamResponse getById(Long id);

    Page<TeamSummaryResponse> list(String search, Pageable pageable);

    void softDelete(Long id);
}
