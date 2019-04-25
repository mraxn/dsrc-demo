@Echo off
REM echo Running MyCloud DL server on port 50020... 
TITLE MyCloud DL
:Start
java -jar MyCloudDL-0.1-jar-with-dependencies.jar 50020 50021 192.168.0.10
echo Program terminated at %Date% %Time% with Error %ErrorLevel% >> .\crash_report.log
goto Start