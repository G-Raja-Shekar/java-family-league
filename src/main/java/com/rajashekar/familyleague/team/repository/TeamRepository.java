package com.rajashekar.familyleague.team.repository;

import com.rajashekar.familyleague.team.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);

    Page<Team> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
