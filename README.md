# CDSHooksCards

```
{
  "resourceType" : "Questionnaire",
  "id" : [{ Identifier }],          // Additional identifier for the questionnaire
  "title" : "<string>",             // Name for this questionnaire (human friendly)
  "status" : "<code>",              // R!  draft | active | retired | unknown
  "item" : [{                       // C? Questions and sections within the Questionnaire
    "linkId" : "<string>",          // R!  Unique id for item in questionnaire
    "text" : "<string>",            // Primary text for the item
    "type" : "<code>",              // R!  group | display | boolean | decimal | integer | date | dateTime +
  }]
}
```
____
Example questionRequest
```
{
  "hook": "patient-info",
  "hookInstance": "1234567890",
  "fhirServer": "http://their_url.com",
  "user": "Screener",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "male",
      "birthDate": "1925-12-23",
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
    }
  }
}
```
____
Example questionResponse
```
{
  "hook": "chlamydia-screening",
  "hookInstance": "1234567890",
  "fhirServer": "http://our_url.com",
  "user": "Practitioner",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "male",
      "birthDate": "1925-12-23",
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
____
Example Request
```
{
  "hook": "chlamydia-screening",
  "hookInstance": "1234567890",
  "fhirServer": "http://our_url.com",
  "user": "Practitioner",
  "prefetch": {
    "patient": {
      "resourceType": "Patient",
      "gender": "male",
      "birthDate": "1925-12-23",
      "id": "1288992",
      "active": true
    }
  }
}
```
____
Example cardsResponse
```
{
  "cards": [
    {
      "summary": "Screening required",
      "indicator": "info",
      "source": "ChlamydiaScreeningCDS.cql",
      "detail": "Due to the bla-bla the screening is required."
    }
  ]
}
```
