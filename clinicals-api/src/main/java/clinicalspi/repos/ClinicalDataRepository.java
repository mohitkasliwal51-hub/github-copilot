package clinicalspi.repos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import clinicalspi.models.ClinicalData;
import clinicalspi.models.Patient;

@Repository
public interface ClinicalDataRepository extends JpaRepository<ClinicalData, Long> {
    List<ClinicalData> findByPatient(Patient patient);

    List<ClinicalData> findByComponentNameIgnoreCase(String componentName);
}