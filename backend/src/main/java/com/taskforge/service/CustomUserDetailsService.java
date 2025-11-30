package com.taskforge.service;

import com.taskforge.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service personnalisé implémentant l'interface UserDetailsService de Spring Security.
 * Ce service est utilisé pour charger les données spécifiques de l'utilisateur lors de l'authentification
 * en interrogeant la base de données via UserRepository.
 */
@Service 
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Recherche un utilisateur dans la base de données par son nom d'utilisateur.
     * Cette méthode est appelée par Spring Security lors du processus d'authentification
     * pour récupérer les détails de l'utilisateur (mot de passe, autorités, etc.).
     *
     * @param username Le nom d'utilisateur à rechercher.
     * @return L'objet UserDetails correspondant à l'utilisateur trouvé.
     * @throws UsernameNotFoundException Si aucun utilisateur n'est trouvé avec ce nom.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}