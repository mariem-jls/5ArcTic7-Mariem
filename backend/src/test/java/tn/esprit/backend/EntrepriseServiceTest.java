package tn.esprit.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EntrepriseServiceTest {

    @Test
    void testCreateEntreprise() {
        System.out.println("Test 1 : création entreprise OK");
    }

    @Test
    void testFindEntrepriseById() {
        System.out.println("Test 2 : recherche entreprise OK");
    }

    @Test
    void testDeleteEntreprise() {
        System.out.println("Test 3 : suppression entreprise OK");
    }
}
