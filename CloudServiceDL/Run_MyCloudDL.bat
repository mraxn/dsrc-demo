@Echo off
REM echo Running MyCloud DL server on port 50020... 
TITLE MyCloud DL
:Start
java -jar MyCloudDL.jar 50020 <SERVER_IP> 7800 <SERVER_IP> 7900
echo Program terminated at %Date% %Time% with Error %ErrorLevel% >> .\crash_report.log
goto Start