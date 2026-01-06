package org.example.duelmasters.Repositories;

import org.example.duelmasters.Models.Civilization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CivilizationRepository extends JpaRepository<Civilization, Integer> {
}
