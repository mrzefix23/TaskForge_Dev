package com.taskforge.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.repositories.ProjectRepository;
import com.taskforge.repositories.UserRepository;
import java.util.List;
import java.util.ArrayList;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

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

    public List<Project> getProjectsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Project> ownedProjects = projectRepository.findProjectsByOwnerId(user.getId());
        List<Project> memberProjects = projectRepository.findAllProjectsByMemberId(user.getId());

        Set<Project> allProjects = new HashSet<>(ownedProjects);
        allProjects.addAll(memberProjects);
        return new ArrayList<>(allProjects);
    }
}
