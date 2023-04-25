## Interface Documentation:

### Description:

This interface creates a new alarm rule with the given `AlarmRuleDTO` data.

### Request

`POST /webapi/alarmRule/create`

### Request Headers

- `Authorization` - Token containing authorization data for the request.

### Request Body

- `AlarmRuleDTO` - An AlarmRuleDTO object contains details of the alarm rule.

### Response:

- `Result` - An object containing the response data.

### Response Body:

- `Long` - The id of the newly created alarm rule.

### Request Example:

`curl -i -H "Authorization: <your_token_here>" -H "Content-Type: application/json" -X POST --data '{"ruleName": "test_rule", "alarmLevel": "Critical", "rule": "", "timeFilter": ">=5", "status": true, "recover": true, "isMerge": false}' http://localhost:8080/webapi/alarmRule/create`

### Response Example:

```
{
  "code": "200",
  "message": "success",
  "data": 1
}
```
