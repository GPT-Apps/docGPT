# Interface Documentation: Custom Plugin Page Query

This interface provides a pagination query functionality to retrieve a list of custom plugins based on the provided search criteria.

## Request

| Name           | Type   | Description                                           | Example                |
| -------------- | ------ | ----------------------------------------------------- | ---------------------- |
| pageNum        | int    | The page number requested. Default is 1.              | 2                      |
| pageSize       | int    | The number of results per page. Default is 10.        | 20                     |
| sortBy         | string | The field to sort results by.                          | "name"                 |
| sortRule       | string | The rule by which to sort results. Ascending ("asc") or descending ("desc"). | "asc"                  |
| target         | object | The search criteria.                                  | See CustomPluginDTO     |
| from           | long   | The start time to filter custom plugins by. (UNIX timestamp in milliseconds) | 1625087600000          |
| to             | long   | The end time to filter custom plugins by. (UNIX timestamp in milliseconds) | 1625097600000          |

### CustomPluginDTO Object

| Name           | Type   | Description                                           | Example                |
| -------------- | ------ | ----------------------------------------------------- | ---------------------- |
| id             | long   | The unique identifier for the custom plugin.          | 1                      |
| tenant         | string | The tenant for which the custom plugin was created.   | "acme-corporation"     |
| workspace      | string | The workspace in which the custom plugin was created. | "production"           |
| parentFolderId | long   | The ID of the parent folder that contains the custom plugin. | 42 |
| name           | string | The name of the custom plugin.                         | "My Sample Plugin"     |
| pluginType     | string | The type of the custom plugin.                         | "regex-matcher"        |
| status         | object | The status of the custom plugin.                       | See CustomPluginStatus |
| periodType     | object | The period type of the custom plugin.                  | See CustomPluginPeriodType |
| conf           | object | The configuration settings for the custom plugin.      | See CustomPluginConf   |
| sampleLog      | string | A sample log entry to use for testing the custom plugin. | "This is a sample log entry." |
| creator        | string | The username of the creator of the custom plugin.      | "jane_doe"             |
| modifier       | string | The username of the last user to modify the custom plugin. | "john_smith"            |
| gmtCreate      | date   | The date and time the custom plugin was created.       | "2021-07-01T16:30:00Z" |
| gmtModified    | date   | The date and time the custom plugin was last modified. | "2021-07-05T10:45:00Z" |


## Response

The response body is a JSON object containing the following fields.

| Name    | Type   | Description                                           |
| ------- | ------ | ----------------------------------------------------- |
| code    | int    | A status code indicating success or failure.          |
| message | string | A message explaining the response status.             |
| data    | object | A paginated result set of custom plugins.              |

### Sample Response Payload

```
{
    "code": 200,
    "message": "success",
    "data": {
        "pageNum": 1,
        "pageSize": 10,
        "size": 2,
        "startRow": 1,
        "endRow": 2,
        "total": 2,
        "pages": 1,
        "list": [
            {
                "id": 1,
                "tenant": "acme-corporation",
                "workspace": "production",
                "parentFolderId": 42,
                "name": "My Sample Plugin",
                "pluginType": "regex-matcher",
                "status": {
                    "value": "published",
                    "label": "Published"
                },
                "periodType": {
                    "value": "day",
                    "label": "Day"
                },
                "conf": {
                    "pattern": "^.*$",
                    "group": "0"
                },
                "sampleLog": "This is a sample log entry.",
                "creator": "jane_doe",
                "modifier": "jane_doe",
                "gmtCreate": "2021-07-01T16:30:00Z",
                "gmtModified": "2021-07-01T16:45:00Z"
            },
            {
                "id": 2,
                "tenant": "acme-corporation",
                "workspace": "production",
                "parentFolderId": 42,
                "name": "My Second Plugin",
                "pluginType": "custom-class",
                "status": {
                    "value": "draft",
                    "label": "Draft"
                },
                "periodType": {
                    "value": "hour",
                    "label": "Hour"
                },
                "conf": {
                    "classPath": "com.mycompany.myapp.MyCustomPlugin",
                    "params": {
                        "param1": "value1",
                        "param2": "value2"
                    }
                },
                "sampleLog": "This is a sample log entry.",
                "creator": "jane_doe",
                "modifier": "john_smith",
                "gmtCreate": "2021-07-02T09:00:00Z",
                "gmtModified": "2021-07-05T10:45:00Z"
            }
        ],
        "prePage": 0,
        "nextPage": 0,
        "isFirstPage": true,
        "isLastPage": true,
        "hasPreviousPage": false,
        "hasNextPage": false,
        "navigatePages": 8,
        "navigatepageNums": [
          1
        ]
    }
}
```

## Example

### Request

```
curl -XPOST \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer {YOUR JWT TOKEN}" \
    -d '{
        "pageNum": 1,
        "pageSize": 10,
        "sortBy": "name",
        "sortRule": "asc",
        "target": {
            "name": "My",
            "pluginType": "regex-matcher"
        },
        "from": 1625087600000,
        "to": 1625097600000
    }' \
    https://example.com/webapi/customPlugin/pageQuery
```

### Response

See the "Sample Response Payload" under the Response section.