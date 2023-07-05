#colors for bash
RED='\033[0;31m'
NOCOLOR='\033[0m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
#/.fastlane/bin/dependencies/include/openssl
#lines to adapt

KEYTOOLCOMMAND="/Library/Java/JavaVirtualMachines/corretto-1.8.0_362/Contents/Home/bin/keytool"
OPENSSLCOMMAND="openssl"

ROOTFODER='assets'
SUBFOLDER='keys'
FILENAME='bnkeystore'
PASSWORD='changeit-Important!'
CERTPATH='certs'
DAYSVALID=7600


DOMAIN='localhost'
COUNTRY='DE'
LOCATION='Munich'
STATE='Germany'
LOCALITY='Bavaria'
ORGANISATION='Organization'
ORGANISATIONALUNIT='Bladenight-Munich-Event'
COMMONNAME='bladenight-muenchen.de'

printf "${RED}Alert.\nDo you want to create new certificate files FILENAME $FILENAME?.\nThis will override old files and can not be undone! Enter: yes to start :"
read -p "?" asking
if [ "$asking" = "yes" ] || [ "$asking" = "y" ]; then
    printf "${GREEN}OK...Let us go ...${NOCOLOR}\n"
else
   printf "${RED}Canceled...Let us stop ...${NOCOLOR}\n"
   exit 1
fi

printf "${GREEN}\nEnter always the same password and TRUST YES on asking!!!${NOCOLOR}"


#assetfolder for app
mkdir $ROOTFODER
cd $ROOTFODER
mkdir ${CERTPATH}
printf 'Create key folder %s\n' "$SUBFOLDER"
mkdir ${SUBFOLDER}
cd ${SUBFOLDER}
rm ${FILENAME}.*

# Create csr conf

cat > csr.conf <<EOF
[ req ]
default_bits = 2048
prompt = no
default_md = sha256
req_extensions = req_ext
distinguished_name = dn

[ dn ]
C = ${COUNTRY}      #Country (C)
ST = ${STATE}       #State (S)
L = ${LOCALITY}     #Locality (L)
O = ${ORGANISATION} #Organization (O)
OU = ${ORGANISATIONALUNIT}   #Organizational Unit (OU)
CN = ${COMMONNAME}      #Common Name (CN)

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = ${DOMAIN}
DNS.2 = www.${DOMAIN}
IP.1 = 127.0.0.1

EOF

#keytool -genkey -alias sitename -keyalg RSA -keystore keystore.jks -keysize 2048.
printf "${YELLOW}Start creating...\n\n${NOCOLOR}"
mkdir ${CERTPATH}

#https://wiki.eclipse.org/Jetty/Tutorial/Embedding_Jetty#Setting_Contexts
#https://www.eclipse.org/jetty/documentation/jetty-9/index.html#advanced-extras
#https://stackoverflow.com/questions/34756062/setting-up-secure-websocket-server-with-jetty-and-javascript-client/37882046#37882046
printf "${GREEN}Create ${FILENAME}\n${NOCOLOR}"
# Generate private key
printf "${GREEN}Write keyfile %s.key${NOCOLOR}\n" "$FILENAME"
openssl genrsa -des3 -out ${FILENAME}.key -passout pass:${PASSWORD}

printf "${GREEN}Write keyfile %s.crt${NOCOLOR}\n" "$FILENAME"
openssl req -new -x509 -key ${FILENAME}.key -out ${FILENAME}.crt -subj "/CN=${DOMAIN}/C=${COUNTRY}/L=${LOCATION}" -days ${DAYSVALID} -passin pass:${PASSWORD} -passout pass:${PASSWORD}

printf "${GREEN}Create keystore ${FILENAME}.crt${NOCOLOR}\n"
${KEYTOOLCOMMAND} -keystore ${FILENAME}.keystore -import -alias ${ORGANISATION} -validity ${DAYSVALID} -file ${FILENAME}.crt -trustcacerts -storepass ${PASSWORD} -deststorepass ${PASSWORD} -noprompt
#${KEYTOOLCOMMAND} -keystore keystore -alias jetty -genkey -keyalg RSA -validity 3600 -storepass ${PASSWORD} -deststorepass ${PASSWORD}

printf "${GREEN}Create csr ${FILENAME}.csr${NOCOLOR}\n"
openssl req -new -key ${FILENAME}.key -out ${FILENAME}.csr -subj "/CN=${DOMAIN}/C=${COUNTRY}/L=${LOCATION}" -passin pass:${PASSWORD} -passout pass:${PASSWORD} -batch

printf "${GREEN}Create ${FILENAME}.key${NOCOLOR}\n"
openssl pkcs12 -inkey ${FILENAME}.key -in ${FILENAME}.crt -export -out ${FILENAME}.pkcs12 -passin pass:${PASSWORD} -passout pass:${PASSWORD}

printf "${GREEN}Merge keystore ${FILENAME}.csr${NOCOLOR}\n"
${KEYTOOLCOMMAND} -importkeystore -srckeystore ${FILENAME}.pkcs12 -srcstoretype PKCS12 -destkeystore $CERTPATH/${FILENAME}.pkcs12 -storepass ${PASSWORD} -deststorepass ${PASSWORD} -srcstorepass ${PASSWORD} -noprompt
cp ${FILENAME}.pkcs12 ../$CERTPATH/${FILENAME}.pkcs12

printf "${RED}Use $ROOTFODER/$CERTPATH/$FILENAME.pkcs12 for server and keep password and other files in ${CERTPATH} and keys on a safe place\n"