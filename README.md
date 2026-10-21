## Résultat de l'exécution

<img width="1270" height="674" alt="1" src="image/Capture d'écran 2026-10-16 221655.png" />

<img width="1270" height="674" alt="1" src="image/Capture d'écran 2026-10-16 221700.png" />


# API Banque

Tests du service REST de gestion des comptes bancaires (Spring Boot, JAX-RS/Jersey, H2).

**URL de base :** `http://localhost:8082/banque`
**Formats :** JSON (par défaut) et XML (`Accept: application/xml`)

| Méthode | URL | Action |
|---|---|---|
| GET | `/comptes` | Lister les comptes |
| GET | `/comptes/{id}` | Détail d'un compte |
| POST | `/comptes` | Créer un compte |
| PUT | `/comptes/{id}` | Modifier un compte |
| DELETE | `/comptes/{id}` | Supprimer un compte |

**Exemple de corps (POST / GET / PUT) :**

<img width="1270" height="674" alt="1" src="image/Capture d'écran 2026-10-16 221706.png" />

<img width="1270" height="674" alt="1" src="image/Capture d'écran 2026-10-16 221722.png" />

<img width="1270" height="674" alt="1" src="image/Capture d'écran 2026-10-16 221729.png" />

