# Discipline App

Application de gestion de la discipline des enfants développée avec Spring Boot 3.

## Technologies utilisées

- Java 21
- Spring Boot 3.4.3
- Spring Security 6
- Thymeleaf
- PostgreSQL
- Maven
- HTMX
- Tailwind CSS

## Prérequis

- JDK 21
- Maven 3.8+
- PostgreSQL 15+

## Configuration

1. Cloner le projet :
```bash
git clone https://github.com/votre-username/discipline.git
cd discipline
```

2. Configurer la base de données :
- Créer une base de données PostgreSQL nommée `discipline`
- Modifier les paramètres de connexion dans `src/main/resources/application.yml` si nécessaire

3. Variables d'environnement :
```properties
POSTGRES_URL=jdbc:postgresql://localhost:5432/discipline
POSTGRES_USER=votre_utilisateur
POSTGRES_PASSWORD=votre_mot_de_passe
ADMIN_USERNAME=admin
ADMIN_PASSWORD=mot_de_passe_admin
```

## Démarrage

```bash
mvn spring-boot:run
```

L'application sera accessible à l'adresse : http://localhost:8080

## Tests

```bash
mvn test
```

## CI/CD

Le projet utilise GitHub Actions pour :
- Compiler le projet
- Exécuter les tests
- Générer les artefacts

## Licence

Ce projet est sous licence MIT. 