package com.taskforge.models;

import jakarta.persistence.*;
import lombok.*;

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
    private UserStory userStory;
    
    @ManyToOne
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    public enum Status {
        TODO, IN_PROGRESS, DONE
    }
}