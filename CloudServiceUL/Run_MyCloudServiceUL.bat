@Echo off
TITLE MyCloud UL
echo Started MyCloud UL server... 
:Start
java -jar MyCloudUL.jar <SERVER_IP> 7800 <SERVER_IP> 7900
echo Program terminated at %Date% %Time% with Error %ErrorLevel% >> .\crash_report.log
goto Start