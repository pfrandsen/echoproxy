#!/bin/bash

# use echoproxy and Java wsdl compiler (wsimport) to find list of url's that wsdl uses

pushd ..
mvn clean package
clear

port=1337
key=7331
echoLog=target/echo.log
cmd1="java -jar target/echoproxy-1.0.jar -port ${port} -echoFile ${echoLog} -shutdownKey 7331"

echo "Starting echo proxy: ${cmd1}"
`exec x-terminal-emulator 1>&2 -e $cmd1`
echo
sleep 2s # let proxy start (separate shell)

mkdir target/out1
name1="OIOAbonnentVirksomhedStamOplysningHent"
wsimport -d target/out1 -quiet -Xnocompile -httpproxy:localhost:${port} http://85.81.229.78/services/prod/ES/wsdl/OIOAbonnentVirksomhedStamOplysningHent.wsdl
mv ${echoLog} target/${name1}.log

mkdir target/out2
name2="OIOAbonnentCVRSENummerRelationHent"
wsimport -d target/out2 -quiet -Xnocompile -httpproxy:localhost:${port} http://85.81.229.78/services/prod/ES/wsdl/OIOAbonnentCVRSENummerRelationHent.wsdl
mv ${echoLog} target/${name2}.log

count=`cat target/${name1}.log | wc -l`
echo "${name1} - ${count} request including wsdl, see target/${name1}.log for list"
count=`cat target/${name2}.log | wc -l`
echo "${name2} - ${count} request including wsdl, see target/${name2}.log for list"

# send shutdown signal to echo proxy
wget --tries=1 -q localhost:${port}/shutdown/${key}
echo
popd

# example wsdl's that include resources
#
# http://85.81.229.78/services/demo/TaxAnnualDetails/TaxAnnualDetails.wsdl
# http://85.81.229.78/services/demo/Tinglysning/StorkundeMultiHent.wsdl
# http://85.81.229.78/services/demo/eIndkomst2/wsdl/IndkomstOplysningPersonAdvisBestil.wsdl
# http://85.81.229.78/services/demo/Intrastat/OIOVirksomhedSoeg.wsdl
# http://85.81.229.78/services/prod/ES/wsdl/OIOAbonnentVirksomhedStamOplysningHent.wsdl
# http://85.81.229.78/services/prod/eIndkomst/wsdl/AnsaettelseForholdPersonHent.wsdl
# http://85.81.229.78/services/prod/eIndkomst/wsdl/IndkomstOplysningPersonHent.wsdl
# http://85.81.229.78/services/prod/ES/wsdl/OIOAbonnentCVRSENummerRelationHent.wsdl
# http://85.81.229.78/services/prod/PersonKontrolOplysningHent/PersonKontrolOplysningHent.wsdl
