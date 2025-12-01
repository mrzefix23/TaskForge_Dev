package com.taskforge.security;

import com.taskforge.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtre de sécurité exécuté une fois par requête pour gérer l'authentification JWT.
 * Il intercepte les requêtes HTTP, extrait le jeton JWT de l'en-tête "Authorization",
 * valide le jeton et configure le contexte de sécurité Spring si l'utilisateur est authentifié.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Logique principale du filtre.
     * Vérifie la présence et la validité du token JWT. Si valide, authentifie l'utilisateur
     * dans le contexte de sécurité de Spring.
     *
     * @param request  La requête HTTP entrante.
     * @param response La réponse HTTP sortante.
     * @param chain    La chaîne de filtres à poursuivre.
     * @throws ServletException En cas d'erreur de servlet.
     * @throws IOException      En cas d'erreur d'entrée/sortie.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7);
            final String username = jwtService.extractUsername(token); 

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}