package com.taskforge.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Map;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.Jwts;


/**
 * Service utilitaire pour la gestion des JSON Web Tokens (JWT).
 * Ce service permet de générer des tokens signés pour l'authentification,
 * ainsi que d'extraire des informations (claims) à partir de tokens existants.
 */
@Service
public class JwtService {

    @Value("${security.jwt.expiration-ms}")
    private Long jwtExpirationMs;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    /**
     * Génère un token JWT pour un utilisateur donné.
     *
     * @param userDetails Les détails de l'utilisateur pour lequel le token est généré.
     * @return La chaîne de caractères représentant le token JWT signé.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token JWT avec des claims (informations) supplémentaires.
     *
     * @param extraClaims Une map contenant les claims personnalisés à ajouter au payload du token.
     * @param userDetails Les détails de l'utilisateur.
     * @return La chaîne de caractères représentant le token JWT signé.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails.getUsername(), jwtExpirationMs);
    }

    /**
     * Construit le token JWT en définissant ses propriétés (claims, sujet, dates, signature).
     *
     * @param extraClaims  Les claims supplémentaires.
     * @param subject      Le sujet du token (généralement le nom d'utilisateur).
     * @param expirationMs La durée de validité du token en millisecondes.
     * @return Le token JWT compacté et signé.
     */
    private String buildToken(Map<String, Object> extraClaims, String subject, Long expirationMs) {
        long now = System.currentTimeMillis();
        return io.jsonwebtoken.Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new java.util.Date(now))
                .setExpiration(new java.util.Date(now + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Récupère la clé cryptographique utilisée pour signer les tokens.
     * La clé est décodée depuis la configuration (Base64).
     *
     * @return La clé secrète (SecretKey).
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrait le nom d'utilisateur (sujet) contenu dans un token JWT.
     *
     * @param token Le token JWT.
     * @return Le nom d'utilisateur.
     */
    public String extractUsername(String token) {
        return extractClaim(token, io.jsonwebtoken.Claims::getSubject);
    }

    /**
     * Extrait une information spécifique (claim) du token en utilisant une fonction de résolution.
     *
     * @param token          Le token JWT.
     * @param claimsResolver La fonction permettant d'extraire le type désiré à partir des Claims.
     * @param <T>            Le type de la donnée extraite.
     * @return La donnée extraite.
     */
    public <T> T extractClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> claimsResolver) {
        final io.jsonwebtoken.Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse le token JWT pour en extraire l'ensemble des claims (le corps du token).
     * Cette méthode vérifie également la signature du token.
     *
     * @param token Le token JWT.
     * @return L'objet Claims contenant toutes les informations du payload.
     * @throws io.jsonwebtoken.JwtException Si le token est invalide ou expiré.
     */
    private io.jsonwebtoken.Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
