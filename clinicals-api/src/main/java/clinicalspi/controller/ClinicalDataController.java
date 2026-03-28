package clinicalspi.controller;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import clinicalspi.dto.ClinicalDataRequest;
import clinicalspi.models.ClinicalData;
import clinicalspi.models.Patient;
import clinicalspi.repos.ClinicalDataRepository;
import clinicalspi.repos.PatientRepository;

@RestController
@RequestMapping("/api/clinical-data")
@Validated
public class ClinicalDataController {

    private final ClinicalDataRepository clinicalDataRepository;
    private final PatientRepository patientRepository;

    public ClinicalDataController(ClinicalDataRepository clinicalDataRepository,
            PatientRepository patientRepository) {
        this.clinicalDataRepository = clinicalDataRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClinicalData>> findAll() {
        return ResponseEntity.ok(clinicalDataRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClinicalData> findById(@PathVariable Long id) {
        return clinicalDataRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ClinicalData>> findByPatient(@PathVariable Long patientId) {
        return patientRepository.findById(patientId)
                .map(clinicalDataRepository::findByPatient)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/component")
    public ResponseEntity<List<ClinicalData>> findByComponent(@RequestParam String componentName) {
        return ResponseEntity.ok(clinicalDataRepository.findByComponentNameIgnoreCase(componentName));
    }

    @PostMapping("/patient/{patientId}")
    public ResponseEntity<ClinicalData> create(@PathVariable Long patientId,
            @RequestBody @Validated ClinicalData payload) {
        return patientRepository.findById(patientId)
                .map(patient -> {
                    payload.setId(null);
                    payload.setPatient(patient);
                    ClinicalData saved = clinicalDataRepository.save(payload);
                    return ResponseEntity
                            .created(URI.create("/api/clinical-data/" + saved.getId()))
                            .body(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClinicalData> update(@PathVariable Long id,
            @RequestBody @Validated ClinicalData payload) {
        return clinicalDataRepository.findById(id)
                .map(existing -> {
                    existing.setComponentName(payload.getComponentName());
                    existing.setComponentValue(payload.getComponentValue());
                    existing.setMeasuredDateTime(payload.getMeasuredDateTime());
                    return ResponseEntity.ok(clinicalDataRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return clinicalDataRepository.findById(id)
                .map(entity -> {
                    clinicalDataRepository.delete(entity);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // method that receives patient ID and clinical data and saves it to the
    // database
    @PostMapping("/patient/{patientId}/clinical-data")
    public ResponseEntity<ClinicalData> saveClinicalData(
            @PathVariable("patientId") Long patientId,
            @RequestBody @Validated ClinicalDataRequest request) {

        return patientRepository.findById(patientId)
                .map(patient -> {
                    ClinicalData clinicalData = new ClinicalData();
                    clinicalData.setComponentName(request.getComponentName());
                    clinicalData.setComponentValue(request.getComponentValue());

                    Timestamp measured = Timestamp.valueOf(
                            LocalDateTime.parse(request.getMeasuredDateTime()));
                    clinicalData.setMeasuredDateTime(measured);

                    clinicalData.setPatient(patient);
                    ClinicalData saved = clinicalDataRepository.save(clinicalData);
                    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
