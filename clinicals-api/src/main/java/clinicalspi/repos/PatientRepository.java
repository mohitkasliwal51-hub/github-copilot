package clinicalspi.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import clinicalspi.models.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByFirstNameAndLastName(String firstName, String lastName);

    List<Patient> findByAgeGreaterThanEqual(Integer age);
}