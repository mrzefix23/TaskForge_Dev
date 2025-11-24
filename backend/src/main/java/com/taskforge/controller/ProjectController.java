package com.taskforge.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.CreateProjectRequest;
import com.taskforge.models.Project;
import com.taskforge.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody CreateProjectRequest createProjectRequest, Principal principal) {
        if(principal == null || !createProjectRequest.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Project project = projectService.createProject(createProjectRequest);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<Project> updateProject(@PathVariable Long projectId, @RequestBody CreateProjectRequest updateRequest, Principal principal){
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        Project updatedProject = projectService.updateProject(projectId, principal.getName(), updateRequest);
        
        return ResponseEntity.ok(updatedProject);

    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long projectId, Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        Project project = projectService.getProjectById(projectId, principal.getName());
        return ResponseEntity.ok(project);
    }

    @GetMapping("/myprojects")
    public ResponseEntity<List<Project>> getMyProjects(Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        List<Project> projects = projectService.getProjectsByUsername(principal.getName());
        return ResponseEntity.ok(projects);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId, Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        projectService.deleteProject(projectId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}