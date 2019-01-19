#!/usr/bin/env bash
#/bin/bash
#This script is a slightly modified and automated version of the gencerts bash script that can be found
# here: https://github.com/aqnouch/BeatTheMeddler
#
# This script creates a root CA and server certificates to be used by the client and the server.
# rootCA.crt needs to be installed in the client.
# keystore.jks needs to be referenced by keyStorePath in the server's config file
#
# ALTNAME can be either a DNS name or an IP
#  ALTNAME=DNS:myhost.org ./gencerts.sh
#  ALTNAME=IP:10.1.4.218 ./gencerts.sh


# Make sure this script does not get run by accident, require the user to manually delete the artifacts

for file in keystore.jks rootCA.crt
do
  if [ -f "$file" ]; then
    echo "$file already exists - it must be deleted prior to running this script"
    exit 1
  fi
done

# The rootCA and client certs will expire after this time period.
#
CERT_LIFE_IN_DAYS=3650

# Environmental Variables and their default values if not set prior to running this script:
: "${ALTNAME:=DNS:praisewm.org}"
: "${BASENAME:=praisewm}"
: "${KEYSTORE_PWD:=praisewm}"
: "${S_C:=US}"
: "${S_ST:=CA}"
: "${S_L:=San Diego}"
: "${S_O:=praisewm Certificate Authority}"
: "${S_OU:=}"
: "${S_CN:=praisewm.org}"

SUBJECT="/C=${S_C}/ST=${S_ST}/L=${S_L}/O=${S_O}/OU=${S_OU}/CN=${S_CN}"

# Create private key for root CA certificate
openssl genrsa -out rootCA.key 4096

# Create a self-signed root CA certificate
openssl req -x509 -new -nodes -days ${CERT_LIFE_IN_DAYS}  -out rootCA.crt -key rootCA.key -subj "${SUBJECT}"

# Create server certificate key
openssl genrsa -out ${BASENAME}.key 4096

# Create Certificate Signing Request
openssl req -new -subj "${SUBJECT}" -key ${BASENAME}.key -out ${BASENAME}.csr

# Sign the certificate with the root CA

openssl x509 -req -in ${BASENAME}.csr -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -days ${CERT_LIFE_IN_DAYS} -out ${BASENAME}.crt  -extensions extensions -extfile <(cat <<-EOF
[ extensions ]
basicConstraints=CA:FALSE
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
subjectAltName=$ALTNAME
EOF
)

# Export to host key and certificate to PKCS12 format which is recognized by Java keytool
openssl pkcs12 -export -password pass:"${KEYSTORE_PWD}" -in ${BASENAME}.crt -inkey ${BASENAME}.key -out keystore.p12 -name "${BASENAME} Certificate Authority" -CAfile rootCA.crt

# Import the host key and certificate to Java keystore format for use by the server
keytool -importkeystore -srcstoretype PKCS12 -srckeystore keystore.p12 -srcstorepass "${KEYSTORE_PWD}" -destkeystore keystore.jks -deststorepass "${KEYSTORE_PWD}"

#${BASENAME}.store must be placed into  Android client
#keytool -importcert -v -trustcacerts -file ${BASENAME}.crt -alias IntermediateCA -keystore ${BASENAME}.store -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath ~/data/java/Signal_prj/bcprov-jdk15on-154.jar -storetype BKS -storepass "${KEYSTORE_PWD}"

#for iOS client you have to convert  ${BASENAME}.crt in  DER format and install ${BASENAME}.cer in Signal-iOS
#openssl x509 -in ${BASENAME}.crt -out ${BASENAME}.cer -outform DER

# Removing all files we created that we do not need
rm -f ${BASENAME}.crt ${BASENAME}.csr ${BASENAME}.key keystore.p12 rootCA.key rootCA.srl
