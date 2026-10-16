package com.example;

import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;
import org.h2.tools.Server;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {

        // ----- Console H2 (au début) -----
        try {
            Server.createWebServer("-web", "-webPort", "8082").start();
            System.out.println("Console H2 disponible sur : http://localhost:8082");
        } catch (Exception e) {
            System.out.println("Erreur lors du démarrage de la console H2");
            e.printStackTrace();
        }

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");

        DataInitService dataInitService = new DataInitService();
        PerformanceTestService performanceTestService = new PerformanceTestService();

        dataInitService.emf = emf;
        performanceTestService.emf = emf;

        dataInitService.initData();
        System.out.println("===== Données : 10 auteurs, 50 livres =====");
        System.out.println();

        performanceTestService.testPerformanceSansCache();
        performanceTestService.testPerformanceAvecJoinFetch();
        performanceTestService.testPerformanceAvecEntityGraph();
        performanceTestService.testPerformanceAvecCache();

        // ----- Garder la console H2 ouverte (à la fin) -----
        System.out.println("Appuyez sur Entrée pour arrêter le programme et la console H2...");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
        emf.close();
        System.exit(0);
    }
}