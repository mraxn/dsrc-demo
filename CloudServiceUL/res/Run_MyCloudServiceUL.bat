@Echo off
TITLE MyCloud UL
echo Started MyCloud UL server... 
:Start
java -jar MyCloudUL-0.1-jar-with-dependencies.jar 192.168.0.10
echo Program terminated at %Date% %Time% with Error %ErrorLevel% >> .\crash_report.log
goto Start