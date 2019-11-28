# CDSHooksCards test project  
  
### This hook provides decision making on Chlamydia Screening test proposal  
  
This decision making is based on ChlamydiaScreeningCDS cql-library.  
  
#### Request  
  
Expected request to Chlamydia Screening Test service has to 
satisfy CDS Hooks specification as well as contain Patient FHIR resource in prefetch.  
Additionally to the Patient resource, Observation and DiagnosticReport FHIR resources might be provided 
if the patient had such observations, diagnostics:
Observation resource, referred to "Risk Evaluation, Document: Sexually Transmitted Infection"
DiagnosticReport resource, referred to "Laboratory Test, Result: Chlamydia Screening"  
  
This data has to be sent in HTTP-request body to to the url: http://our_url.com/request  
  
The example of such request with only Patient resource is listed below:
  
```
{
  "hook": "chlamydia-screening",
  "hookInstance": "1234567890",
  "fhirServer": "http://our_url.com",
  "user": "Practitioner",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "female",
      "birthDate": "1995-12-23",
      "id": "1288992",
      "active": true
    }
  }
}
```
  
The example of such request with Patient, Observation and DiagnosticReport resources is listed below:  
  
```
{
  "hook": "chlamydia-screening",
  "hookInstance": "1234567890",
  "fhirServer": "http://our_url.com",
  "user": "Practitioner",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "female",
      "birthDate": "1995-12-23",
      "id": "1288992",
      "active": true
    },
    "observation": {
      "resourceType": "Observation",
      "status": "preliminary",
      "code": "Risk Evaluation, Document: Sexually Transmitted Infection",
      "observedAtTime": "2018-12-12"
    },
    "diagnosticReport": {
      "resourceType": "DiagnosticReport",
      "status": "preliminary",
      "code": "Laboratory Test, Result: Chlamydia Screening",
      "observedAtTime": "2018-03-12",
      "value": "Negative"
    }
  }
}
```
  
For more information refer to:  
- https://cds-hooks.org/  
- https://www.hl7.org/FHIR/patient.html  
- https://www.hl7.org/fhir/observation.html  
- https://www.hl7.org/fhir/diagnosticreport.html  
  
#### Question  
  
In response to the request, Questionnaire FHIR resource will be sent 
to specify certain details in patient's condition.  
  
The example of such questionnaire request is listed below:  
  
```
{
  "hook": "patient-info",
  "hookInstance": "1234567890",
  "fhirServer": "http://their_url.com",
  "user": "Screener",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "female",
      "birthDate": "1995-12-23",
      "id": "1288992",
      "active": true
    },
    "questionnaire": {
      "resourceType": "Questionnaire",
      "id": "qId0",
      "title": "Pregnancy",
      "status": "draft",
      "item": [
        {
          "linkId": "lId0",
          "text": "Did the patient take a Chlamydia Screening test?",
          "type": "boolean"
        }
      ]
    }
  }
}
```
  
For more information refer to:  
- https://www.hl7.org/fhir/questionnaire.html  
  
#### Response  
  
QuestionnaireResponse FHIR resource is expected in the next HTTP-request 
sent to to the url: http://our_url.com/answer  
It has to contain both Patient and relevant Questionnaire resources along with
QuestionnaireResponse in the prefetch.  
  
The example of such QuestionnaireResponse response is listed below:  
  
```
{
  "hook": "chlamydia-screening",
  "hookInstance": "1234567890",
  "fhirServer": "http://our_url.com",
  "user": "Practitioner",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "female",
      "birthDate": "1995-12-23",
      "id": "1288992",
      "active": true
    },
    "questionnaire": {
      "resourceType": "Questionnaire",
      "id": "qId0",
      "title": "Pregnancy",
      "status": "draft",
      "item": [
        {
          "linkId": "lId0",
          "text": "Is the patient pregnant?",
          "type": "boolean"
        }
      ]
    },
    "questionnaireresponse": {
      "resourceType" : "QuestionnaireResponse",
      "id" : "questionnaireresponse1",
      "questionnaire" : "questionnaire/qId0",
      "status" : "completed",
      "authored" : "2019-07-18",
      "item" : [
        {
          "linkId" : "lId0",
          "text" : "Is the patient pregnant?",
          "answer" : [
            {
              "valueBoolean" : "True"
            }
          ]
        }
      ]
    }
  }
}
```
  
For more information refer to:  
- https://www.hl7.org/fhir/questionnaireresponse.html  
  
#### Result  
  
In case when all required information is gathered, CDS Cards will be sent in the
response to the previous HTTP-request.  
  
The example of such CDS Cards response is listed below:  
  
```
{
  "cards": [
    {
      "summary": "Chlamydia Screening test required",
      "indicator": "info",
      "source": "ChlamydiaScreeningCDS.cql",
      "detail": "Patient is at risk for chlamydia infection by age group, no screening test available in past 1 year"
    }
  ]
}
```
  
For more information refer to:  
- https://cds-hooks.org/#cds-cards  
- https://cds-hooks.org/specification/1.0/  
