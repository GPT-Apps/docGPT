## Interface Documentation for Custom Plugin Creation

This interface creates a custom plugin with the given custom plugin data.

### Endpoint

POST `/webapi/customPlugin/create`

### Headers

| Name | Type | Description |
|------|------|-------------|
| Content-Type | string | MIME type of the request payload (application/json) |

### Request Body

CustomPluginDTO: 
- id: Long
- tenant: String
- workspace: String
- parentFolderId: Long
- name: String
- pluginType: String
- status: CustomPluginStatus
- periodType: CustomPluginPeriodType
- conf: CustomPluginConf
- sampleLog: String
- creator: String
- modifier: String
- gmtCreate: Date
- gmtModified: Date

#### Example Request Body

```json
{
  "id": 1,
  "tenant": "test",
  "workspace": "test",
  "parentFolderId": 123,
  "name": "My Custom Plugin",
  "pluginType": "java",
  "status": "ENABLED",
  "periodType": "DAILY",
  "conf": {
    "config1": "value1",
    "config2": "value2"
  },
  "sampleLog": "Sample log for the custom plugin",
  "creator": "user1",
  "modifier": "user1",
  "gmtCreate": "2021-01-01T00:00:00Z",
  "gmtModified": "2021-01-01T00:00:00Z"
}
```

### Response

JsonResult<CustomPluginDTO>:
- code: integer
- message: string
- data: CustomPluginDTO

#### Example Response

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "tenant": "test",
    "workspace": "test",
    "parentFolderId": 123,
    "name": "My Custom Plugin",
    "pluginType": "java",
    "status": "ENABLED",
    "periodType": "DAILY",
    "conf": {
      "config1": "value1",
      "config2": "value2"
    },
    "sampleLog": "Sample log for the custom plugin",
    "creator": "user1",
    "modifier": "user1",
    "gmtCreate": "2021-01-01T00:00:00Z",
    "gmtModified": "2021-01-01T00:00:00Z"
  }
}
```

### Curl Example

```
curl -X POST -H "Content-Type:application/json" -d '{"id":1,"tenant":"test","workspace":"test","parentFolderId":123,"name":"My Custom Plugin","pluginType":"java","status":"ENABLED","periodType":"DAILY","conf":{"config1":"value1","config2":"value2"},"sampleLog":"Sample log for the custom plugin","creator":"user1","modifier":"user1","gmtCreate":"2021-01-01T00:00:00Z","gmtModified":"2021-01-01T00:00:00Z"}' http://example.com/webapi/customPlugin/create
```
