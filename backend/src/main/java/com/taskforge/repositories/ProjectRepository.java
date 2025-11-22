package com.taskforge.repositories;

import com.taskforge.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :memberId")
    List<Project> findAllProjectsByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId")
    List<Project> findProjectsByOwnerId(@Param("ownerId") Long ownerId);
}