package com.rajashekar.familyleague.player.repository;

import com.rajashekar.familyleague.player.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Page<Player> findByTeamId(Long teamId, Pageable pageable);

    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Player> findByTeamIdAndNameContainingIgnoreCase(Long teamId, String name, Pageable pageable);
}
