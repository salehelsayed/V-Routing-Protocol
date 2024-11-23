@echo off
echo Testing V-Routing Protocol APIs
echo ============================
echo.

set BASE_URL=http://localhost:8080/api

echo Testing /test endpoint...
curl -X GET "%BASE_URL%/test"
echo.
echo.

echo Starting a simulation...
curl -X POST "%BASE_URL%/simulation/start" ^
-H "Content-Type: application/json" ^
-d "{\"nodeCount\": 10}" > simulation_response.txt

set /p SIMULATION_ID=<simulation_response.txt
echo Simulation ID: %SIMULATION_ID%
echo.

echo Getting current status...
curl -X GET "%BASE_URL%/simulation/status"
echo.
echo.

echo Getting specific simulation status...
curl -X GET "%BASE_URL%/simulation/%SIMULATION_ID%/status"
echo.
echo.

echo Getting current metrics...
curl -X GET "%BASE_URL%/simulation/metrics"
echo.
echo.

echo Getting specific simulation metrics...
curl -X GET "%BASE_URL%/simulation/%SIMULATION_ID%/metrics"
echo.
echo.

echo Stepping simulation...
curl -X POST "%BASE_URL%/simulation/%SIMULATION_ID%/step"
echo.
echo.

echo Getting updated status...
curl -X GET "%BASE_URL%/simulation/status"
echo.
echo.

echo Stopping simulation...
curl -X POST "%BASE_URL%/simulation/%SIMULATION_ID%/stop"
echo.
echo.

echo Testing complete!
echo Results will indicate which endpoints are working and which are not.
echo Check the responses above for each endpoint.

del simulation_response.txt
