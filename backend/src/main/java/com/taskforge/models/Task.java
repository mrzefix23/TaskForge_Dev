package com.taskforge.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité représentant une tâche technique.
 * Une tâche est une sous-unité de travail liée à une User Story spécifique.
 * Elle possède un statut, une priorité et peut être assignée à un utilisateur.
 */
@Entity
@Table(name = "tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
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
    @JoinColumn(name = "user_story_id", nullable = false)
    @JsonBackReference
    private UserStory userStory;
    
    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id")
    @JsonIgnore
    private User assignedTo;
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    public enum Status {
        TODO, IN_PROGRESS, DONE
    }
}