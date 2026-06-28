package com.example.imagemanager.repository;

import com.example.imagemanager.entity.FolderPreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderPresetRepository extends JpaRepository<FolderPreset, Long> {

    List<FolderPreset> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FolderPreset> findByIdAndUserId(Long id, Long userId);
}
