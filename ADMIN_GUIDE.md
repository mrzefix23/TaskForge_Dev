# Documentation Administrateur - TaskForge

**Version :** 0.2.0
**Date de mise à jour :** 11-30-2025

## 1. Présentation
TaskForge est une application de gestion de projet centralisée. Ce document décrit les procédures d'installation, de configuration et de maintenance pour le déploiement de l'application en environnement de production ou de pré-production via Docker.

---

## 2. Prérequis Serveur

L'application est conçue pour être conteneurisée. Le serveur hôte doit disposer de :

* **Système d'exploitation :** Linux (Ubuntu, Debian, CentOS recommandés) ou Windows Server.
* **Docker Engine :** Version 20.10 ou supérieure.
* **Docker Compose :** Version 1.29 ou supérieure.
* **Git :** Pour la récupération des sources.

### Ports requis
Assurez-vous que les ports suivants sont ouverts et libres sur le serveur :

| Port | Service | Description |
| :--- | :--- | :--- |
| **80** | Frontend (Nginx) | Accès web utilisateurs (HTTP) |
| **8080** | Backend API | Communication API |
| **5432** | PostgreSQL | Base de données (optionnel si accès externe requis) |

---

## 3. Installation et Déploiement

### 3.1. Récupération des sources
Connectez-vous au serveur et clonez le dépôt :

```bash
git clone [https://github.com/mrzefix23/TaskForge_Dev.git](https://github.com/mrzefix23/TaskForge_Dev.git)
cd TaskForge_Dev
```

### 3.2. Configuration de l'environnement
**⚠️ Important :** Pour la sécurité, ne jamais utiliser les configurations par défaut en production.

1.  **Générer les fichiers de configuration** à partir des exemples :
    ```bash
    cp backend/src/main/resources/.env.example backend/src/main/resources/.env
    cp .env.example .env
    ```

2.  **Éditer le fichier `.env`** situé à la racine du projet :
    ```ini
    # Configuration de la Base de Données
    POSTGRES_USER=admin_prod            # Définir un nom d'utilisateur sécurisé
    POSTGRES_PASSWORD=ChangeMeSecure!   # Définir un mot de passe fort
    POSTGRES_DB=taskforge_prod
    ```

### 3.3. Lancement de l'application
Utilisez Docker Compose pour construire les images et lancer les conteneurs en mode détaché (arrière-plan) :

```bash
docker-compose up -d --build
```

### 3.4. Vérification
Vérifiez que les trois services (frontend, backend, db) sont actifs (Statut "Up") :

```bash
docker-compose ps
```

---

## 4. Accès à l'application

Une fois les services démarrés, l'application est accessible via :

* **Interface Utilisateur :** `http://<IP-DU-SERVEUR>/`
* **API Swagger (Test technique) :** `http://<IP-DU-SERVEUR>:8080/swagger-ui.html`

---

## 5. Maintenance Courante

### 5.1. Gestion du cycle de vie

**Arrêter l'application :**
```bash
docker-compose down
```

**Redémarrer l'application :**
```bash
docker-compose restart
```

**Mettre à jour l'application (après un changement de code) :**
```bash
git pull origin main
docker-compose up -d --build
```

### 5.2. Consultation des Logs
Pour diagnostiquer des erreurs (ex: erreur 500, problèmes de connexion) :

**Voir les logs de tous les services en temps réel :**
```bash
docker-compose logs -f
```

**Voir uniquement les logs du backend (API) :**
```bash
docker-compose logs -f backend
```

**Voir uniquement les logs de la base de données :**
```bash
docker-compose logs -f db
```

**Voir uniquement les logs du frontend (Nginx) :**
```bash
docker-compose logs -f frontend
```

---

## 6. Sauvegarde et Restauration

Les données sont stockées dans un volume Docker persistant.

### Sauvegarde de la Base de Données (Dump SQL)
Pour effectuer une sauvegarde à chaud sans arrêter le service :

```bash
# Syntaxe : docker-compose exec db pg_dump -U <USER> <DB_NAME> > fichier.sql
docker-compose exec db pg_dump -U admin_prod taskforge_prod > backup_taskforge_$(date +%F).sql
```
*(Remplacez `admin_prod` et `taskforge_prod` par les valeurs définies dans votre `.env`)*

### Restauration de la Base de Données
```bash
cat backup_taskforge_YYYY-MM-DD.sql | docker-compose exec -T db psql -U admin_prod taskforge_prod
```

---

## 7. Dépannage (Troubleshooting)

| Problème | Cause Possible | Solution |
| :--- | :--- | :--- |
| **Site inaccessible** | Conteneurs arrêtés ou port 80 bloqué | Vérifier `docker-compose ps` et le pare-feu du serveur. |
| **Erreur de connexion DB** | Mauvais mot de passe dans `.env` | Vérifier que les variables `POSTGRES_PASSWORD` sont identiques dans le fichier `.env` et la config Spring Boot. |
| **Erreur 500 au Login** | Backend non prêt | Attendre quelques secondes que Spring Boot ait fini de démarrer (voir logs). |
| **Connexion refusée** | Base de données non initialisée | Vérifier les logs DB : `docker-compose logs db`. |