package com.taskforge.controller;

import com.taskforge.models.User;
import com.taskforge.service.UserService;
import com.taskforge.dto.UserDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;

/**
 * Contrôleur REST gérant les opérations de lecture sur les utilisateurs.
 * Fournit des points de terminaison pour récupérer la liste des utilisateurs ou un utilisateur spécifique.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Récupère la liste complète de tous les utilisateurs.
     *
     * @return Une liste d'objets UserDto représentant tous les utilisateurs.
     */
    @Operation(summary = "Récupérer tous les utilisateurs", description="Récupère une liste de tous les utilisateurs.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateurs récupérés avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès refusé")
    })
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un utilisateur spécifique par son identifiant.
     *
     * @param id L'identifiant unique de l'utilisateur.
     * @return L'objet UserDto correspondant, ou null si non trouvé.
     */
    @Operation(summary = "Récupérer un utilisateur par ID", description="Récupère les détails d'un utilisateur spécifique en fonction de son ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur récupéré avec succès"),
        @ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? toUserDto(user) : null;
    }

    /**
     * Convertit une entité User en objet de transfert de données (DTO).
     * Masque le mot de passe pour des raisons de sécurité.
     *
     * @param user L'entité utilisateur à convertir.
     * @return Le DTO correspondant.
     */
    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        // never expose password
        dto.setPassword(null);
        return dto;
    }
}