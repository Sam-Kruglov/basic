# Sources:
# https://cloud.google.com/iot/docs/how-tos/credentials/keys#generating_an_rsa_key_with_a_self-signed_x509_certificate
# https://adangel.org/2016/08/29/openssl-rsa-java/

# generate private key
openssl genpkey -algorithm RSA -out rsa_private_pkcs1.pem -pkeyopt rsa_keygen_bits:2048
# extract public key from the private key. Base64 content is supported by Java's X509EncodedKeySpec.
openssl rsa -in rsa_private_pkcs1.pem -pubout -out rsa_public.pem
# translate private key from the default PKCS1 format
# into PKCS8 that is supported by Java's PKCS8EncodedKeySpec (also in Base64).
openssl pkcs8 -topk8 -in rsa_private_pkcs1.pem -out rsa_private.pem -nocrypt
# delete the PKCS1 private key version
rm rsa_private_pkcs1.pem
