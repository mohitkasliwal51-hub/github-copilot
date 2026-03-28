import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link, useParams } from 'react-router-dom';

const AddClinicals = ({ }) => {
  const [patient, setPatient] = useState(null);
  const {patientId} = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchPatient = async () => {
      try {
        const response = await axios.get(`http://localhost:8080/clinicalsapi/patients/${patientId}`);
        setPatient(response.data);
        setError(null);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (patientId) {
      fetchPatient();
    }
  }, [patientId]);

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Patient Details</h2>
      {patient.firstName ? (
        <>
          <p><strong>First Name:</strong> {patient.firstName}</p>
          <p><strong>Last Name:</strong> {patient.lastName}</p>
          <p><strong>Age:</strong> {patient.age}</p>
        
        <form>
          <h3>Add Clinical Data</h3>
          Compont Name: <input type="text" name="componentName" /><br />
          Component Value: <input type="text" name="componentValue" /><br />
        <button type="button" onClick={() => {
          const componentName = document.querySelector('input[name="componentName"]').value;
          const componentValue = document.querySelector('input[name="componentValue"]').value;
          const clinicalData = {
            patientId: patientId,
            componentName: componentName,
            componentValue: componentValue
          };
          axios.post('http://localhost:8080/clinicalsapi/clinicaldata/clinicals', clinicalData)
            .then(response => {
              alert('Clinical data added successfully!');
            })
            .catch(error => {
              alert('Error adding clinical data: ' + error.message);
            });
        }
        }>Add Clinical Data</button>
        </form>
        <Link to="/">Back to Patient List</Link>
        </>
      ) : (
        <p>Loading patient data ...</p>
      )}
      
    </div>
  );
};

export default AddClinicals;