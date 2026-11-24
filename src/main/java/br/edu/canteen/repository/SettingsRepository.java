package br.edu.canteen.repository;

import br.edu.canteen.model.CanteenSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<CanteenSettings, Long> {
}
