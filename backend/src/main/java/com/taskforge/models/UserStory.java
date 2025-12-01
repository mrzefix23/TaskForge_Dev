package com.taskforge.models;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entité représentant une User Story.
 * Une User Story décrit une fonctionnalité ou un besoin du point de vue de l'utilisateur final.
 * Elle est liée à un projet et peut être décomposée en plusieurs tâches techniques.
 */
@Entity
@Table(name = "user_stories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne
    @JoinColumn(name = "sprint_id", nullable = true)
    private Sprint sprint;
    
    @ManyToMany
    @JoinTable(
        name = "user_story_assignees",
        joinColumns = @JoinColumn(name = "user_story_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedTo = new HashSet<>();
    
    @OneToMany(mappedBy = "userStory", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Task> tasks;

    @ManyToOne
    @JoinColumn(name = "version_id", nullable = true)
    @JsonIgnoreProperties({"userStories", "project"})
    private Version version;
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    public enum Status {
        TODO, IN_PROGRESS, DONE
    }
}