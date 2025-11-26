package com.taskforge.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.exceptions.DuplicateProjectNameException;
import com.taskforge.exceptions.ProjectSuppressionException;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserRepository;
import com.taskforge.repositories.UserStoryRepository;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    public Project createProject(CreateProjectRequest createProjectRequest) {
        User owner = userRepository.findByUsername(createProjectRequest.getUser().getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create the project
        Project project = Project.builder()
                .name(createProjectRequest.getName())
                .description(createProjectRequest.getDescription())
                .owner(owner)
                .build();

        Set<User> members = new HashSet<>();
        // Add members to the project
        if(createProjectRequest.getMembers() != null) {
            for (var memberDto : createProjectRequest.getMembers()) {
                User member = userRepository.findByUsername(memberDto.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found: " + memberDto.getUsername()));
                members.add(member);
            }
        }
        // Add owner as a member
        members.add(owner);

        // Set members to project
        project.setMembers(members);
        
        return projectRepository.save(project);
    }

    public Project getProjectById(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        boolean isMember = project.getMembers().stream()
                .anyMatch(member -> member.getUsername().equals(username));

        if (!isMember) {
            throw new RuntimeException("User is not a member of this project");
        }

        return project;
    }

    public Project updateProject(Long projectId, String username, CreateProjectRequest updateRequest) {
        Project project = getProjectById(projectId, username);

        // Vérifier si le projet existe déjà
        projectRepository.findByName(updateRequest.getName()).filter(existingProject -> !existingProject.getId().equals(projectId))
            .ifPresent(existingProject -> {
                throw new DuplicateProjectNameException("Un projet avec ce nom existe déjà.");
            });
        
        project.setName(updateRequest.getName());
        project.setDescription(updateRequest.getDescription());
        
        Set<User> members = new HashSet<>();
        if(updateRequest.getMembers() != null) {
            for (var memberDto : updateRequest.getMembers()) {
                User member = userRepository.findByUsername(memberDto.getUsername())
                        .orElseThrow(() -> new RuntimeException("User not found: " + memberDto.getUsername()));
                members.add(member);
            }
        }
        // Ensure owner is still a member
        members.add(project.getOwner());
        
        project.setMembers(members);
        
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId, String username) {
        Project project = getProjectById(projectId, username);
        if (!project.getOwner().getUsername().equals(username)) {
            throw new ProjectSuppressionException("Uniquement le propriétaire du projet peut le supprimer.");
        }

        //Supprimer les US lié au projet
        userStoryRepository.deleteAllByProjectId(projectId);

        projectRepository.deleteById(projectId);
    }   

    public List<Project> getProjectsByUsername(String username) {
        return projectRepository.findAllByOwnerOrMember(username);
    }
}
