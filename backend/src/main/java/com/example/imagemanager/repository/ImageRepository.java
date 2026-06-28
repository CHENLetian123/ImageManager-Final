package com.example.imagemanager.repository;

import com.example.imagemanager.entity.ImageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<ImageItem, Long>, JpaSpecificationExecutor<ImageItem> {

    Optional<ImageItem> findByIdAndUserId(Long id, Long userId);

    @Query("select distinct i.sourceName from ImageItem i where i.userId = :userId and i.sourceName is not null and i.sourceName <> '' order by i.sourceName")
    List<String> findDistinctSourceNamesByUserId(@Param("userId") Long userId);
}
