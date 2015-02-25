echo off
REM use echoproxy and Java wsdl compiler (wsimport) to find list of url's that wsdl uses
pushd ..
call mvn clean package
cls

echo Starting echo proxy

start cmd /c "title=echoproxy-1337 & java -jar target/echoproxy-1.0.jar -httpProxy webproxy:8080 -port 1337 -echoFile target\echo.log -shutdownKey 7331"
timeout 3 1> nul
echo Starting analysis

mkdir target\out1
set name1=OIOAbonnentVirksomhedStamOplysningHent
echo Analyzing %name1%.wsdl
call wsimport -d target\out1 -quiet -Xnocompile -httpproxy:localhost:1337 http://85.81.229.78/services/prod/ES/wsdl/OIOAbonnentVirksomhedStamOplysningHent.wsdl
move target\echo.log target\%name1%.log 1> nul

mkdir target\out2
set name2=OIOAbonnentCVRSENummerRelationHent
echo Analyzing %name2%.wsdl
call wsimport -d target\out2 -quiet -Xnocompile -httpproxy:localhost:1337 http://85.81.229.78/services/prod/ES/wsdl/OIOAbonnentCVRSENummerRelationHent.wsdl
move target\echo.log target\%name2%.log 1> nul
echo.
<nul set /p notused="Resources used by %name1%: "
type target\%name1%.log | find /c /v ""
echo See details in target\%name1%.log
<nul set /p notused="Resources used by %name2%: "
type target\%name2%.log | find /c /v ""
echo See details in target\%name2%.log

REM tasklist /FI "WINDOWTITLE eq echoproxy-1337*" /FI "IMAGENAME eq cmd.exe"
echo.
echo Stopping echo proxy
taskkill /FI "WINDOWTITLE eq echoproxy-1337*" /FI "IMAGENAME eq cmd.exe" 1> nul
popd
echo on