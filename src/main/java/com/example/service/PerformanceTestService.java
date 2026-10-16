package com.example.service;

import com.example.model.Auteur;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Supplier;

public class PerformanceTestService {

    public EntityManagerFactory emf;

    private SessionFactory sf() {
        return emf.unwrap(SessionFactory.class);
    }

    /** Exécute l'action puis affiche durée, requêtes SQL et statistiques de cache. */
    private void mesurer(String titre, Supplier<String> action) {
        Statistics stats = sf().getStatistics();
        stats.clear();

        long debut = System.nanoTime();
        String detail = action.get();
        long ms = (System.nanoTime() - debut) / 1_000_000;

        System.out.println(titre);
        System.out.println("   Résultat        : " + detail);
        System.out.println("   Durée           : " + ms + " ms");
        System.out.println("   Requêtes SQL    : " + stats.getPrepareStatementCount());
        System.out.println("   Cache 2e niveau : " + stats.getSecondLevelCacheHitCount() + " hits, "
                + stats.getSecondLevelCacheMissCount() + " miss");
        System.out.println("   Cache requêtes  : " + stats.getQueryCacheHitCount() + " hit, "
                + stats.getQueryCacheMissCount() + " miss");
        System.out.println();
    }

    private String resume(List<Auteur> auteurs) {
        int livres = 0;
        for (Auteur auteur : auteurs) {
            livres += auteur.getLivres().size();   // déclenche le chargement LAZY si nécessaire
        }
        return auteurs.size() + " auteurs, " + livres + " livres";
    }

    /** 1. Problème N+1 : 1 requête pour les auteurs, puis 1 par auteur pour ses livres. */
    public void testPerformanceSansCache() {
        sf().getCache().evictAllRegions();

        mesurer("1. Sans optimisation (problème N+1)", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                List<Auteur> auteurs = em.createQuery("SELECT a FROM Auteur a", Auteur.class)
                        .getResultList();
                return resume(auteurs);
            } finally {
                em.close();
            }
        });
    }

    /** 2. JOIN FETCH : auteurs et livres en une seule requête. */
    public void testPerformanceAvecJoinFetch() {
        // Doublons sans DISTINCT (non mesuré)
        sf().getCache().evictAllRegions();
        EntityManager em0 = emf.createEntityManager();
        try {
            int n = em0.createQuery("SELECT a FROM Auteur a JOIN FETCH a.livres", Auteur.class)
                    .getResultList().size();
            System.out.println("(Sans DISTINCT, la liste contient " + n + " éléments : un par ligne de jointure)");
            System.out.println();
        } finally {
            em0.close();
        }

        sf().getCache().evictAllRegions();
        mesurer("2. JOIN FETCH avec DISTINCT", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                List<Auteur> auteurs = em.createQuery(
                                "SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class)
                        .getResultList();
                return resume(auteurs);
            } finally {
                em.close();
            }
        });
    }

    /** 3. Entity Graph : même résultat, sans modifier la requête JPQL. */
    public void testPerformanceAvecEntityGraph() {
        sf().getCache().evictAllRegions();

        mesurer("3. Entity Graph (Auteur.livres)", () -> {
            EntityManager em = emf.createEntityManager();
            try {
                EntityGraph<?> graph = em.createEntityGraph("Auteur.livres");

                List<Auteur> auteurs = em.createQuery("SELECT DISTINCT a FROM Auteur a", Auteur.class)
                        .setHint("javax.persistence.fetchgraph", graph)
                        .getResultList();
                return resume(auteurs);
            } finally {
                em.close();
            }
        });
    }

    /** 4. Cache de second niveau : cache de requêtes + entités + collections. */
    public void testPerformanceAvecCache() {
        sf().getCache().evictAllRegions();

        Supplier<String> lecture = () -> {
            EntityManager em = emf.createEntityManager();
            try {
                List<Auteur> auteurs = em.createQuery("SELECT a FROM Auteur a", Auteur.class)
                        .setHint("org.hibernate.cacheable", true)
                        .getResultList();
                return resume(auteurs);
            } finally {
                em.close();
            }
        };

        // Première lecture - chargement depuis la base
        mesurer("4a. Cache de 2e niveau, lecture 1 (cache vide)", lecture);

        // Deuxième lecture - chargement depuis le cache
        mesurer("4b. Cache de 2e niveau, lecture 2 (cache chaud)", lecture);
    }
}