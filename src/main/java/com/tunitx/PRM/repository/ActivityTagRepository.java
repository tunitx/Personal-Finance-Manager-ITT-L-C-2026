package com.tunitx.PRM.repository;

import com.tunitx.PRM.model.ActivityTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityTagRepository
        extends JpaRepository<ActivityTag, Long> {

    List<ActivityTag> findAllByIdIn(List<Long> ids);
}