package clinicalspi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import clinicalspi.dto.ClinicalDataRequest;
import clinicalspi.models.ClinicalData;
import clinicalspi.models.Patient;
import clinicalspi.repos.ClinicalDataRepository;
import clinicalspi.repos.PatientRepository;

@WebMvcTest(ClinicalDataController.class)
class ClinicalDataControllerTest {

	private static final String BASE_URL = "/api/clinical-data";
	private static final long PATIENT_ID = 5L;
	private static final Timestamp DEFAULT_TIMESTAMP = Timestamp.valueOf(LocalDateTime.of(2024, 2, 10, 10, 0));

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ClinicalDataRepository clinicalDataRepository;

	@MockBean
	private PatientRepository patientRepository;

	@Test
	void findAllReturnsListOfClinicalData() throws Exception {
		ClinicalData data = clinicalData(1L);
		when(clinicalDataRepository.findAll()).thenReturn(List.of(data));

		mockMvc.perform(get(BASE_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].componentName").value("BP"))
				.andExpect(jsonPath("$[0].componentValue").value("120/80"));
	}

	@Test
	void findByIdReturnsEntity() throws Exception {
		ClinicalData data = clinicalData(10L, "BP", "120/80", patient(3L));
		when(clinicalDataRepository.findById(10L)).thenReturn(Optional.of(data));

		mockMvc.perform(get(BASE_URL + "/{id}", 10L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10L))
				.andExpect(jsonPath("$.componentValue").value("120/80"));
	}

	@Test
	void findByIdReturnsNotFoundWhenMissing() throws Exception {
		Long id = 99L;
		when(clinicalDataRepository.findById(id)).thenReturn(Optional.empty());

		mockMvc.perform(get(BASE_URL + "/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void createClinicalDataPersistsWhenPatientExists() throws Exception {
		Long patientId = 5L;
		Patient patient = patient(patientId);
		when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
		when(clinicalDataRepository.save(any(ClinicalData.class))).thenAnswer(invocation -> {
			ClinicalData saved = invocation.getArgument(0);
			saved.setId(11L);
			return saved;
		});

		ClinicalData payload = clinicalData(null);
		String body = objectMapper.writeValueAsString(payload);

		mockMvc.perform(post(BASE_URL + "/patient/{patientId}", patientId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/clinical-data/11"))
				.andExpect(jsonPath("$.id").value(11L))
				.andExpect(jsonPath("$.componentName").value("BP"));

		ArgumentCaptor<ClinicalData> captor = ArgumentCaptor.forClass(ClinicalData.class);
		verify(clinicalDataRepository).save(captor.capture());
		ClinicalData saved = captor.getValue();
		verify(patientRepository).findById(patientId);
		assertEquals(patientId, saved.getPatient().getId());
	}

	@Test
	void createClinicalDataReturnsNotFoundWhenPatientMissing() throws Exception {
		Long patientId = 8L;
		when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

		mockMvc.perform(post(BASE_URL + "/patient/{patientId}", patientId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isNotFound());

		verify(clinicalDataRepository, never()).save(any(ClinicalData.class));
	}

	@Test
	void findByPatientReturnsData() throws Exception {
		Patient patient = patient(PATIENT_ID);
		List<ClinicalData> payload = List.of(clinicalData(4L, "BP", "120/80", patient));
		when(patientRepository.findById(PATIENT_ID)).thenReturn(Optional.of(patient));
		when(clinicalDataRepository.findByPatient(patient)).thenReturn(payload);

		mockMvc.perform(get(BASE_URL + "/patient/{patientId}", PATIENT_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].componentName").value("BP"));
	}

	@Test
	void findByPatientReturnsNotFoundWhenPatientMissing() throws Exception {
		Long unknownPatient = 99L;
		when(patientRepository.findById(unknownPatient)).thenReturn(Optional.empty());

		mockMvc.perform(get(BASE_URL + "/patient/{patientId}", unknownPatient))
				.andExpect(status().isNotFound());

		verify(clinicalDataRepository, never()).findByPatient(any(Patient.class));
	}

	@Test
	void findByComponentReturnsMatches() throws Exception {
		when(clinicalDataRepository.findByComponentNameIgnoreCase("BP"))
				.thenReturn(List.of(clinicalData(6L, "BP", "120/80", patient(2L))));

		mockMvc.perform(get(BASE_URL + "/component").param("componentName", "BP"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].componentValue").value("120/80"));
	}

	@Test
	void saveClinicalDataFromRequestCreatesEntry() throws Exception {
		Long patientId = 7L;
		Patient patient = patient(patientId);
		when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
		when(clinicalDataRepository.save(any(ClinicalData.class))).thenAnswer(invocation -> {
			ClinicalData saved = invocation.getArgument(0);
			saved.setId(21L);
			return saved;
		});

		ClinicalDataRequest request = new ClinicalDataRequest();
		request.setComponentName("HR");
		request.setComponentValue("80");
		String measured = "2025-01-13T09:15:00";
		request.setMeasuredDateTime(measured);

		mockMvc.perform(post(BASE_URL + "/patient/{patientId}/clinical-data", patientId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(21L))
				.andExpect(jsonPath("$.componentName").value("HR"))
				.andExpect(jsonPath("$.componentValue").value("80"));

		ArgumentCaptor<ClinicalData> captor = ArgumentCaptor.forClass(ClinicalData.class);
		verify(clinicalDataRepository).save(captor.capture());
		ClinicalData persisted = captor.getValue();
		Timestamp expectedTimestamp = Timestamp.valueOf(LocalDateTime.parse(measured));
		assertEquals(expectedTimestamp, persisted.getMeasuredDateTime());
	}

	@Test
	void saveClinicalDataReturnsNotFoundWhenPatientMissing() throws Exception {
		Long patientId = 42L;
		when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

		ClinicalDataRequest request = new ClinicalDataRequest();
		request.setComponentName("Temp");
		request.setComponentValue("98.6");
		request.setMeasuredDateTime("2024-02-01T08:00:00");

		mockMvc.perform(post(BASE_URL + "/patient/{patientId}/clinical-data", patientId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound());

		verify(clinicalDataRepository, never()).save(any(ClinicalData.class));
	}

	@Test
	void updateClinicalDataUpdatesFieldsWhenPresent() throws Exception {
		Long id = 12L;
		Patient patient = patient(4L);
		ClinicalData existing = clinicalData(id, "BP", "120/80", patient);
		ClinicalData updated = clinicalData(id, "BP", "130/85", patient);
		when(clinicalDataRepository.findById(id)).thenReturn(Optional.of(existing));
		when(clinicalDataRepository.save(existing)).thenReturn(updated);

		mockMvc.perform(put(BASE_URL + "/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonClinicalData("BP", "130/85")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.componentValue").value("130/85"));

		verify(clinicalDataRepository).save(existing);
	}

	@Test
	void updateClinicalDataReturnsNotFoundWhenMissing() throws Exception {
		when(clinicalDataRepository.findById(55L)).thenReturn(Optional.empty());

		mockMvc.perform(put(BASE_URL + "/{id}", 55L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonClinicalData("BP", "130/85")))
				.andExpect(status().isNotFound());

		verify(clinicalDataRepository, never()).save(any(ClinicalData.class));
	}

	@Test
	void deleteClinicalDataRemovesEntityWhenPresent() throws Exception {
		ClinicalData existing = clinicalData(77L, "BP", "120/80", patient(7L));
		when(clinicalDataRepository.findById(77L)).thenReturn(Optional.of(existing));

		mockMvc.perform(delete(BASE_URL + "/{id}", 77L))
				.andExpect(status().isNoContent());

		verify(clinicalDataRepository).delete(existing);
	}

	@Test
	void deleteClinicalDataReturnsNotFoundWhenMissing() throws Exception {
		when(clinicalDataRepository.findById(88L)).thenReturn(Optional.empty());

		mockMvc.perform(delete(BASE_URL + "/{id}", 88L))
				.andExpect(status().isNotFound());

		verify(clinicalDataRepository, never()).delete(any(ClinicalData.class));
	}

	private ClinicalData clinicalData(Long id) {
		ClinicalData data = new ClinicalData();
		data.setId(id);
		data.setComponentName("BP");
		data.setComponentValue("120/80");
		data.setMeasuredDateTime(DEFAULT_TIMESTAMP);
		return data;
	}

	private ClinicalData clinicalData(Long id, String componentName, String componentValue, Patient patient) {
		ClinicalData data = clinicalData(id);
		data.setComponentName(componentName);
		data.setComponentValue(componentValue);
		data.setPatient(patient);
		return data;
	}

	private Patient patient(Long id) {
		Patient patient = new Patient();
		patient.setId(id);
		patient.setFirstName("Jane");
		patient.setLastName("Doe");
		patient.setAge(40);
		return patient;
	}

	private String jsonClinicalData(String componentName, String componentValue) {
		return "{" +
				"\"componentName\":\"" + componentName + "\"," +
				"\"componentValue\":\"" + componentValue + "\"" +
				"}";
	}
}
