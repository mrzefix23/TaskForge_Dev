package com.taskforge.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.dto.CreateVersionRequest;
import com.taskforge.exceptions.DuplicateProjectNameException;
import com.taskforge.models.Project;
import com.taskforge.models.UserStory;
import com.taskforge.models.Version;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserStoryRepository;
import com.taskforge.repositories.VersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VersionService {

    private final VersionRepository versionRepository;
    private final ProjectRepository projectRepository;
    private final UserStoryRepository userStoryRepository;

    public List<Version> getVersionsByProject(Long projectId) {
        return versionRepository.findByProjectIdOrderByIdDesc(projectId);
    }

    public Version getVersionById(Long id) {
        return versionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Version non trouvée avec l'ID: " + id));
    }

    @Transactional
    public Version createVersion(CreateVersionRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID: " + request.getProjectId()));

        if (versionRepository.existsByProjectIdAndVersionNumber(request.getProjectId(), request.getVersionNumber())) {
            throw new DuplicateProjectNameException("Une version avec ce numéro existe déjà pour ce projet");
        }

        if (versionRepository.findByProjectIdOrderByIdDesc(request.getProjectId()).stream()
                .anyMatch(v -> v.getTitle().equals(request.getTitle()))) {
            throw new DuplicateProjectNameException("Une version avec ce titre existe déjà pour ce projet");
        }

        Version version = Version.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .versionNumber(request.getVersionNumber())
                .project(project)
                .status(Version.VersionStatus.PLANNED)
                .build();

        return versionRepository.save(version);
    }

    @Transactional
    public Version updateVersion(Long id, CreateVersionRequest request) {
        Version version = getVersionById(id);

        if (!version.getVersionNumber().equals(request.getVersionNumber()) &&
            versionRepository.existsByProjectIdAndVersionNumber(version.getProject().getId(), request.getVersionNumber())) {
            throw new DuplicateProjectNameException("Une version avec ce numéro existe déjà pour ce projet");
        }

        version.setTitle(request.getTitle());
        version.setDescription(request.getDescription());
        version.setVersionNumber(request.getVersionNumber());

        return versionRepository.save(version);
    }

    @Transactional
    public Version updateVersionStatus(Long id, Version.VersionStatus status) {
        Version version = getVersionById(id);
        version.setStatus(status);
        
        // Définir la date de release automatiquement
        if (status == Version.VersionStatus.RELEASED && version.getReleaseDate() == null) {
            version.setReleaseDate(LocalDate.now());
        }
        
        return versionRepository.save(version);
    }

    @Transactional
    public void deleteVersion(Long id) {
        Version version = getVersionById(id);
        
        List<UserStory> userStories = userStoryRepository.findByVersionId(id);
        for (UserStory story : userStories) {
            story.setVersion(null);
            userStoryRepository.save(story);
        }
        
        versionRepository.delete(version);
    }

    @Transactional
    public UserStory assignUserStoryToVersion(Long versionId, Long userStoryId) {
        Version version = getVersionById(versionId);
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story non trouvée avec l'ID: " + userStoryId));

        if (!userStory.getProject().getId().equals(version.getProject().getId())) {
            throw new RuntimeException("La User Story n'appartient pas au même projet que la version");
        }

        userStory.setVersion(version);
        return userStoryRepository.save(userStory);
    }

    @Transactional
    public UserStory removeUserStoryFromVersion(Long userStoryId) {
        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new RuntimeException("User Story non trouvée avec l'ID: " + userStoryId));

        userStory.setVersion(null);
        return userStoryRepository.save(userStory);
    }

    public List<UserStory> getUserStoriesByVersion(Long versionId) {
        return userStoryRepository.findByVersionId(versionId);
    }
}