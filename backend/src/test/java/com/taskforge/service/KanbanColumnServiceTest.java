package com.taskforge.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskforge.dto.CreateKanbanColumnRequest;
import com.taskforge.models.KanbanColumn;
import com.taskforge.models.Project;
import com.taskforge.models.User;
import com.taskforge.repositories.KanbanColumnRepository;

@ExtendWith(MockitoExtension.class)
class KanbanColumnServiceTest {
    
    @Mock
    private KanbanColumnRepository kanbanColumnRepository;
    
    @Mock
    private ProjectService projectService;
    
    @InjectMocks
    private KanbanColumnService kanbanColumnService;
    
    private Project testProject;
    private User testUser;
    private KanbanColumn testColumn;
    private CreateKanbanColumnRequest createRequest;
    
    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build();
        
        testProject = Project.builder()
                .id(1L)
                .name("Test Project")
                .description("Description")
                .owner(testUser)
                .build();
        
        testColumn = KanbanColumn.builder()
                .id(1L)
                .name("À faire")
                .status("TODO")
                .order(1)
                .project(testProject)
                .isDefault(false)
                .build();
        
        createRequest = new CreateKanbanColumnRequest();
        createRequest.setName("En cours");
        createRequest.setStatus("IN_PROGRESS");
        createRequest.setOrder(2);
        createRequest.setProjectId(1L);
    }
    
    @Test
    void createKanbanColumn_shouldCreateSuccessfully() {
        // Given
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.existsByStatusAndProjectId("IN_PROGRESS", 1L)).thenReturn(false);
        when(kanbanColumnRepository.save(any(KanbanColumn.class))).thenReturn(testColumn);
        
        // When
        KanbanColumn result = kanbanColumnService.createKanbanColumn(createRequest, "testuser");
        
        // Then
        assertNotNull(result);
        verify(projectService).getProjectById(1L, "testuser");
        verify(kanbanColumnRepository).existsByStatusAndProjectId("IN_PROGRESS", 1L);
        verify(kanbanColumnRepository).save(any(KanbanColumn.class));
    }
    
    @Test
    void createKanbanColumn_withDuplicateStatus_shouldThrowException() {
        // Given
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.existsByStatusAndProjectId("IN_PROGRESS", 1L)).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kanbanColumnService.createKanbanColumn(createRequest, "testuser");
        });
        
        assertTrue(exception.getMessage().contains("existe déjà"));
        verify(kanbanColumnRepository, times(0)).save(any(KanbanColumn.class));
    }
    
    @Test
    void createKanbanColumn_shouldNormalizeStatus() {
        // Given
        createRequest.setStatus("in progress"); // Avec espaces et minuscules
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.existsByStatusAndProjectId("in progress", 1L)).thenReturn(false);
        
        KanbanColumn savedColumn = KanbanColumn.builder()
                .id(1L)
                .name("En cours")
                .status("IN_PROGRESS") // Devrait être normalisé
                .order(2)
                .project(testProject)
                .isDefault(false)
                .build();
        
        when(kanbanColumnRepository.save(any(KanbanColumn.class))).thenReturn(savedColumn);
        
        // When
        KanbanColumn result = kanbanColumnService.createKanbanColumn(createRequest, "testuser");
        
        // Then
        assertNotNull(result);
        assertEquals("IN_PROGRESS", result.getStatus());
    }
    
    @Test
    void getColumnsByProject_shouldReturnAllColumns() {
        // Given
        KanbanColumn column1 = KanbanColumn.builder()
                .id(1L)
                .name("À faire")
                .status("TODO")
                .order(1)
                .project(testProject)
                .isDefault(true)
                .build();
        
        KanbanColumn column2 = KanbanColumn.builder()
                .id(2L)
                .name("En cours")
                .status("IN_PROGRESS")
                .order(2)
                .project(testProject)
                .isDefault(true)
                .build();
        
        KanbanColumn column3 = KanbanColumn.builder()
                .id(3L)
                .name("Terminé")
                .status("DONE")
                .order(3)
                .project(testProject)
                .isDefault(true)
                .build();
        
        List<KanbanColumn> columns = Arrays.asList(column1, column2, column3);
        
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.findByProjectIdOrderByOrderAsc(1L)).thenReturn(columns);
        
        // When
        List<KanbanColumn> result = kanbanColumnService.getColumnsByProject(1L, "testuser");
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("TODO", result.get(0).getStatus());
        assertEquals("IN_PROGRESS", result.get(1).getStatus());
        assertEquals("DONE", result.get(2).getStatus());
        verify(projectService).getProjectById(1L, "testuser");
        verify(kanbanColumnRepository).findByProjectIdOrderByOrderAsc(1L);
    }
    
    @Test
    void updateKanbanColumn_shouldUpdateName() {
        // Given
        createRequest.setName("Nouveau nom");
        
        when(kanbanColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.save(any(KanbanColumn.class))).thenReturn(testColumn);
        
        // When
        KanbanColumn result = kanbanColumnService.updateKanbanColumn(1L, createRequest, "testuser");
        
        // Then
        assertNotNull(result);
        verify(kanbanColumnRepository).findById(1L);
        verify(projectService).getProjectById(1L, "testuser");
        verify(kanbanColumnRepository).save(any(KanbanColumn.class));
    }
    
    @Test
    void updateKanbanColumn_withNonExistentId_shouldThrowException() {
        // Given
        when(kanbanColumnRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kanbanColumnService.updateKanbanColumn(999L, createRequest, "testuser");
        });
        
        assertTrue(exception.getMessage().contains("non trouvée"));
        verify(kanbanColumnRepository, times(0)).save(any(KanbanColumn.class));
    }
    
    @Test
    void updateKanbanColumn_defaultColumn_shouldNotUpdateOrder() {
        // Given
        testColumn.setIsDefault(true);
        createRequest.setOrder(5);
        
        when(kanbanColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.save(any(KanbanColumn.class))).thenReturn(testColumn);
        
        // When
        kanbanColumnService.updateKanbanColumn(1L, createRequest, "testuser");
        
        // Then
        // L'ordre ne devrait pas être modifié pour une colonne par défaut
        verify(kanbanColumnRepository).save(any(KanbanColumn.class));
    }
    
    @Test
    void updateKanbanColumn_customColumn_shouldUpdateOrder() {
        // Given
        testColumn.setIsDefault(false);
        createRequest.setOrder(5);
        
        when(kanbanColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        when(kanbanColumnRepository.save(any(KanbanColumn.class))).thenReturn(testColumn);
        
        // When
        kanbanColumnService.updateKanbanColumn(1L, createRequest, "testuser");
        
        // Then
        verify(kanbanColumnRepository).save(any(KanbanColumn.class));
    }
    
    @Test
    void deleteKanbanColumn_shouldDeleteSuccessfully() {
        // Given
        when(kanbanColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        
        // When
        kanbanColumnService.deleteKanbanColumn(1L, "testuser");
        
        // Then
        verify(kanbanColumnRepository).findById(1L);
        verify(projectService).getProjectById(1L, "testuser");
        verify(kanbanColumnRepository).delete(testColumn);
    }
    
    @Test
    void deleteKanbanColumn_defaultColumn_shouldThrowException() {
        // Given
        testColumn.setIsDefault(true);
        when(kanbanColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
        when(projectService.getProjectById(1L, "testuser")).thenReturn(testProject);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kanbanColumnService.deleteKanbanColumn(1L, "testuser");
        });
        
        assertTrue(exception.getMessage().contains("par défaut"));
        verify(kanbanColumnRepository, times(0)).delete(any(KanbanColumn.class));
    }
    
    @Test
    void deleteKanbanColumn_withNonExistentId_shouldThrowException() {
        // Given
        when(kanbanColumnRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            kanbanColumnService.deleteKanbanColumn(999L, "testuser");
        });
        
        assertTrue(exception.getMessage().contains("non trouvée"));
        verify(kanbanColumnRepository, times(0)).delete(any(KanbanColumn.class));
    }
    
    @Test
    void initializeDefaultColumns_shouldCreateThreeColumns() {
        // Given
        when(kanbanColumnRepository.save(any(KanbanColumn.class)))
                .thenReturn(testColumn);
        
        // When
        kanbanColumnService.initializeDefaultColumns(testProject);
        
        // Then
        verify(kanbanColumnRepository, times(3)).save(any(KanbanColumn.class));
    }
    
    @Test
    void initializeDefaultColumns_shouldCreateCorrectStatuses() {
        // Given
        // Nous allons capturer les colonnes sauvegardées
        when(kanbanColumnRepository.save(any(KanbanColumn.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        kanbanColumnService.initializeDefaultColumns(testProject);
        
        // Then
        // Vérifier que 3 colonnes ont été créées
        verify(kanbanColumnRepository, times(3)).save(any(KanbanColumn.class));
    }
}

