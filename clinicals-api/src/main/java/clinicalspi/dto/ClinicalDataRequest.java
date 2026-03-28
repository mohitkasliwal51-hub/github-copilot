package clinicalspi.dto;

public class ClinicalDataRequest {
    private Long patientId;
    private String componentName;
    private String componentValue;
    private String measuredDateTime;

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentValue() {
        return componentValue;
    }

    public void setComponentValue(String componentValue) {
        this.componentValue = componentValue;
    }

    public String getMeasuredDateTime() {
        return measuredDateTime;
    }

    public void setMeasuredDateTime(String measuredDateTime) {
        this.measuredDateTime = measuredDateTime;
    }
}
