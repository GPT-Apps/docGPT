API Reference: Custom Plugin Update

This API allows updating a custom plugin in the system. It requires authentication and tenant authorization.

Method Signature

```
public JsonResult<CustomPluginDTO> update(@RequestBody CustomPluginDTO customPluginDTO)
```

Annotations

- `@PostMapping("/update")`
- `@ResponseBody`
- `@MonitorScopeAuth(targetType = AuthTargetType.TENANT, needPower = PowerConstants.EDIT)`
- `@RestController`
- `@RequestMapping("/webapi/customPlugin")`

Fields

Here are the fields of `CustomPluginDTO` that are used in this API.

| Field Name | Type | Description |
| ----------| ---- | ----------- |
| id | Long | The unique ID of the custom plugin. |
| tenant | String | The name of the tenant to which the custom plugin belongs. |
| workspace | String | The name of the workspace to which the custom plugin belongs. |
| parentFolderId | Long | The unique ID of the parent folder of the custom plugin. |
| name | String | The name of the custom plugin. |
| pluginType | String | The type of the custom plugin. |
| status | CustomPluginStatus | The status of the custom plugin. |
| periodType | CustomPluginPeriodType | The period type of the custom plugin. |
| conf | CustomPluginConf | The configuration of the custom plugin. |
| sampleLog | String | The sample log of the custom plugin. |
| creator | String | The name of the creator of the custom plugin. |
| modifier | String | The name of the last modifier of the custom plugin. |
| gmtCreate | Date | The time when the custom plugin was created. |
| gmtModified | Date | The time when the custom plugin was last modified. |

Authorization

This API requires authentication and tenant authorization with edit permissions.

Example

```
curl -X POST -H "Authorization: Bearer your_token" -H "Content-Type: application/json" -d '{ "id": 123, "tenant": "test_tenant", "workspace": "test_workspace", "parentFolderId": 456, "name": "test_plugin", "pluginType": "test_type", "status": { "name": "test_status" }, "periodType": { "name": "test_period" }, "conf": { "property1": "value1", "property2": "value2" } }' "https://yourdomain.com/webapi/customPlugin/update"
``` 

Response

The response contains a `JsonResult` object that represents the updated custom plugin. If the update is successful, the status of the `JsonResult` object is "success" and the data field contains the updated custom plugin. Otherwise, the status is "fail" and the message field contains the error message.