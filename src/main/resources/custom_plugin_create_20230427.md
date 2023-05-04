# Interface Documentation

## Request

`POST` `/webapi/customPlugin/create`

## Description

This API creates a new custom plugin in the system.

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| customPluginDTO.parentFolderId | Long | The ID of the folder where the custom plugin should be created. |
| customPluginDTO.name | String | The name of the custom plugin. |
| customPluginDTO.pluginType | String | The type of the custom plugin. |
| customPluginDTO.status | CustomPluginStatus | The status of the custom plugin. |
| customPluginDTO.periodType | CustomPluginPeriodType | The period type of the custom plugin. |
| customPluginDTO.conf | CustomPluginConf | The configuration of the custom plugin. |
| customPluginDTO.sampleLog | String | The sample log of the custom plugin. |
| customPluginDTO.creator | String | The creator of the custom plugin. |
| customPluginDTO.modifier | String | The modifier of the custom plugin. |
| customPluginDTO.gmtCreate | Date | The creation date of the custom plugin. |
| customPluginDTO.gmtModified | Date | The modification date of the custom plugin. |

## Example

```curl
curl -X POST \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer TOKEN_GOES_HERE" \
     -d '{
        "parentFolderId": 1,
        "name": "custom_plugin",
        "pluginType": "type1",
        "status": "ACTIVE",
        "periodType": "DAILY",
        "conf": {
            "config1": "value1",
            "config2": "value2"
        }
     }' \
     https://example.com/webapi/customPlugin/create
```