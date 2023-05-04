# Interface Documentation

## Overview

This document explains the RESTful API interface for `CustomPluginDTO` management. This interface is used to query a custom plugin by page.

## API Endpoint

Base URL: `http://localhost:8080/webapi/customPlugin`

## API Endpoints

HTTP Method | Endpoint | Description
------------|----------|------------
POST | /pageQuery | Query custom plugin by page

## Example

### Query custom plugin by page

**Request**

```sh
curl -X POST \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer {YOUR_ACCESS_TOKEN}" \
     -d '{
     "pageNum": 1,
     "pageSize": 10,
     "sortBy": "name",
     "sortRule": "DESC",
     "target": {
         "id": 1,
         "tenant": "ABC",
         "workspace": "DEV",
         "parentFolderId": 0,
         "name": "MyCustomPlugin",
         "pluginType": "",
         "status": "ACTIVE",
         "periodType": "MINUTE",
         "conf": {},
         "sampleLog": "",
         "creator": "John",
         "modifier": "John",
         "gmtCreate": "2021-01-01 12:00:00",
         "gmtModified": "2021-01-01 12:00:00"
     },
     "from": 1611255741000,
     "to": 1611259341000
 }' \
     http://localhost:8080/webapi/customPlugin/pageQuery
```

**Response**

```json
{
    "status": "SUCCESS",
    "data": {
        "totalCount": 5,
        "list": [
            {
                "id": 1,
                "tenant": "ABC",
                "workspace": "DEV",
                "parentFolderId": 0,
                "name": "MyCustomPlugin",
                "pluginType": "",
                "status": "ACTIVE",
                "periodType": "MINUTE",
                "conf": {},
                "sampleLog": "",
                "creator": "John",
                "modifier": "John",
                "gmtCreate": "2021-01-01 12:00:00",
                "gmtModified": "2021-01-01 12:00:00"
            },
            {
                "id": 2,
                "tenant": "ABC",
                "workspace": "DEV",
                "parentFolderId": 0,
                "name": "MyCustomPlugin2",
                "pluginType": "",
                "status": "ACTIVE",
                "periodType": "MINUTE",
                "conf": {},
                "sampleLog": "",
                "creator": "John",
                "modifier": "John",
                "gmtCreate": "2021-01-02 12:00:00",
                "gmtModified": "2021-01-02 12:00:00"
            },
            {
                "id": 3,
                "tenant": "ABC",
                "workspace": "DEV",
                "parentFolderId": 0,
                "name": "MyCustomPlugin3",
                "pluginType": "",
                "status": "INACTIVE",
                "periodType": "HOUR",
                "conf": {},
                "sampleLog": "",
                "creator": "John",
                "modifier": "John",
                "gmtCreate": "2021-01-03 12:00:00",
                "gmtModified": "2021-01-03 12:00:00"
            }]
    },
    "errMsg": null
}
```

## Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `pageNum` | Integer | The page number to query |
| `pageSize` | Integer | The number of `CustomPluginDTO` objects to return per page |
| `sortBy` | String | The field by which to sort the results. Allowed values: `id`, `tenant`, `workspace`, `parentFolderId`, `name`, `pluginType`, `status`, `periodType`, `creator`, `gmtCreate`, `modifier`, `gmtModified` |
| `sortRule` | String | The sort order. Allowed values: `ASC` or `DESC` |
| `target` | `CustomPluginDTO` | The `CustomPluginDTO` object to query |
| `from` | Long | The start time range in milliseconds |
| `to` | Long | The end time range in milliseconds | 

## Authorization

This API requires a bearer access token obtained using the OAuth2 protocol. The token should be included in the `Authorization` header of the request.
