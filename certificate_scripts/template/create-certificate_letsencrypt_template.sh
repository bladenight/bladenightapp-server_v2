#!/bin/sh

KEYTOOLCOMMAND="/Library/Java/JavaVirtualMachines/corretto-1.8.0_362/Contents/Home/bin/keytool"
OPENSSLCOMMAND="openssl"

#Script for serverpart to create wss certificate from letsencrypt - Script not validated
#  ///usr/local/psa/var/modules/letsencrypt/etc/live/
PASSWORD=setAPAssword
#tempfolder create and clear
mkdir ./tempfolder
rm keystore_letsencrypt.jks
rm fullchain_and_key.p12
#copy actual Letsencrypt fullchain.pem and privkey.pem to a tempfolder
cp /usr/local/psa/var/modules/letsencrypt/etc/live/fullchain.pem ./tempfolder/
cp /usr/local/psa/var/modules/letsencrypt/etc/live/privkey.pem ./tempfolder/

openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out fullchain_and_key.p12 -name jetty -password pass:useANewPassWord
${KEYTOOLCOMMAND} -importkeystore -destkeystore keystore_letsencrypt.jks -srckeystore fullchain_and_key.p12 -alias jetty -storepass $PASSWORD
keytool -import -destkeystore keystore_letsencrypt -file chain.pem -alias root -storepass $PASSWORD
cp keystore_letsencrypt.jks /home/bnapp/bladenightserver/config/assets/certs/keystore_letsencrypt.jks
#cleanup tempfolder
rm -r ./tempfolder
chown userroot:usergroup /bladenightserver/config/assets/certs/keystore_letsencrypt.jks