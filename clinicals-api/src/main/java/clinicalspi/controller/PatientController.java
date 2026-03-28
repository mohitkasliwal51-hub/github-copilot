package clinicalspi.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import clinicalspi.models.Patient;
import clinicalspi.repos.PatientRepository;

@RestController
@RequestMapping("/api/patients")
@Validated
public class PatientController {

    private final PatientRepository patientRepository;

    public PatientController(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> findAll() {
        return ResponseEntity.ok(patientRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> findById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestBody @Validated Patient payload) {
        Patient created = patientRepository.save(payload);
        return ResponseEntity.created(URI.create("/api/patients/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable Long id,
            @RequestBody @Validated Patient payload) {
        return patientRepository.findById(id)
                .map(existing -> {
                    existing.setFirstName(payload.getFirstName());
                    existing.setLastName(payload.getLastName());
                    existing.setAge(payload.getAge());
                    existing.setClinicalData(payload.getClinicalData());
                    return ResponseEntity.ok(patientRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(existing -> {
                    patientRepository.delete(existing);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Patient> findByName(@RequestParam String firstName,
            @RequestParam String lastName) {
        return patientRepository.findByFirstNameAndLastName(firstName, lastName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/age/{threshold}")
    public ResponseEntity<List<Patient>> findByAge(@PathVariable Integer threshold) {
        return ResponseEntity.ok(patientRepository.findByAgeGreaterThanEqual(threshold));
    }
}