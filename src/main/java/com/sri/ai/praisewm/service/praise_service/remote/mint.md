## MINT Data Catalog Integration

1. A query is manually registered using the following form

    https://docs.google.com/forms/d/e/1FAIpQLSdn0utt1rqsGDcVFgAnHvIyfUGefVUrWt039m8nvY1OdioFqQ/viewform

2. An HTTP GET is sent to the following URL, using parameters established when the query was registered.

     http://mint-demo.westus2.cloudapp.azure.com/data_sets

3. An HTTP POST is sent to the following URL, using data contained in the response to the HTTP GET

     http://mint-demo.westus2.cloudapp.azure.com/data_sets/get_location_url

4. The GeoTIFF returned in response to the HTTP POST is processed to obtain final result.


Example:

1. HTTP GET

    http://mint-demo.westus2.cloudapp.azure.com/data_sets?standard_name=one_month_time_integral_of_precipitation_leq_volume_flux&start_time=1999-04-01T00:00:00&end_time=1999-04-30T23:59:59&location=40,0,55,15

2. HTTP POST

    http://mint-demo.westus2.cloudapp.azure.com/data_sets/get_location_url

    data: { “variable_id”: “results.variable_id.value from the HTTP GET response”}

