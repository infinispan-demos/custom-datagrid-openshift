# Encrypting Cluster Communication
Learn how to create OpenShift secrets.

## Environment Variables for Encrypting Communication

Data Grid uses the following environment variables to encrypt communication:

* `HOSTNAME_HTTP` specifies the HTTP service route for the deployment.
* `HOSTNAME_HTTPS` specifies the HTTPS service route for the deployment.
* `HTTPS_KEYSTORE` specifies the name of a keystore that contains an TLS certificate secures communication with the Data Grid application.
* `HTTPS_SECRET` specifies the name of the OpenShift secret that contains the keystore.
* `HTTPS_NAME` sets the name that is associated with the TLS certificate in the keystore.
* `HTTPS_PASSWORD` sets the password for the TLS certificate.

Data Grid uses JGroups technology to encrypt cluster traffic with the following environment variables:

* `JGROUPS_ENCRYPT_KEYSTORE` specifes the name of a keystore that contains a secret key for encrypting cluster traffic.
* `JGROUPS_ENCRYPT_SECRET` specifies the name of the OpenShift secret that contains the keystore.
* `JGROUPS_ENCRYPT_PASSWORD` sets the password for the keystore.

## Generating Certificates and Keystores
Data Grid uses TLS certificates and keystores to encrypt cluster communication.
In the following section, you can create example certificates and keystores
that demonstrate how to encrypt cluster communication.

### Creating a CA-Signed Certificate
To secure Data Grid traffic, the HTTPS keystore must contain a certificate that is signed by a certificate authority.

**IMPORTANT:** In production environments, you should not generate a
certificate authority (CA) but submit your certificate signing request (CSR) to
a verified CA.

1. Generate a CA certificate.
  ```bash
  $ openssl req -new -newkey rsa:4096 -x509 -keyout my-ca.key -out my-ca.crt -days 365 -subj "/CN=my-cert-authority.ca"
  ```
2. Generate a certificate for the HTTPS keystore.
  ```bash
  $ keytool -genkeypair -keyalg RSA -keysize 2048 -dname "CN=rhdg-https-demo.openshift32.example.com" -alias rhdg-https-key -deststoretype pkcs12 -keystore rhdg-https.jks
  ```
3. Generate a CSR for the certificate in the HTTPS keystore.
  ```bash
  $ keytool -certreq -keyalg rsa -alias rhdg-https-key -keystore rhdg-https.jks -file rhdg.csr
  ```

4. Sign the CSR with the CA certificate.
  ```bash
  $ openssl x509 -req -CA my-ca.crt -CAkey my-ca.key -in rhdg.csr -out rhdg-https.crt -days 365 -CAcreateserial
  ```

### Importing Certificates into the HTTPS Keystore
After you sign the certificate in the HTTPS keystore, you must import the certificate along with the CSR and CA certificate.

1. Import the CA into the HTTPS keystore.
  ```bash
  $ keytool -import -file my-ca.crt -alias my-ca.ca -keystore rhdg-https.jks
  ```

2. Import the signed CSR into the HTTPS keystore.
  ```bash
  $ keytool -import -file rhdg-https.crt -alias rhdg-https-key -keystore rhdg-https.jks
  ```

3. Import the CA into the HTTPS keystore.
  ```bash
  $ keytool -import -file my-ca.crt -alias my-ca.ca -keystore truststore.jks
  ```

### Generating a secure key for the JGroups keystore.
Generate a secure in a JGroups keystore as follows:
```bash
$ keytool -genkey -alias jgroups -keystore jgroups.jceks -storetype PKCS12 -keyalg RSA -validity 730 -keysize 2048
```

## Importing the Data Grid HTTPS Application
Import the Data Grid image stream and `HTTPS` template as follows:
```bash
$ oc login -u system:admin

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.1/templates/datagrid72-image-stream.json

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.1/templates/datagrid72-https.json
```

## Creating a Project and Importing Secrets
Create a new project, configure roles, and then import the HTTPS and JGroups keystores as OpenShift secrets.

1. Log in as the developer user and create a new project.
  ```bash
  $ oc login -u developer

  $ oc new-project rhdg-https
  ```

2. Add the view role to the default service account to allow access to resources in your project, which is required to manage the cluster.
  ```bash
  $ oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default -n $(oc project -q)
  ```

3. Create a secret for the HTTPS keystore.
  ```bash
  $ oc create secret generic rhdg-https-secret --from-file=rhdg-https.jks --from-file=truststore.jks
  ```

4. Create a secret for the JGroups keystore.
  ```bash
  $ oc create secret generic https-jgroup-secret --from-file=jgroups.jceks
  ```

5. Link secrets to the default service account.
  ```bash
  $ oc secrets link default https-jgroup-secret rhdg-https-secret
  ```

## Deploying Data Grid with HTTPS
Create a new application from the Data Grid `HTTPS` template and set environment variables as follows:
```bash
$ oc new-app --template=datagrid72-https --name=rhdg-https \
  -e HTTPS_SECRET=rhdg-https-secret \
  -e HTTPS_KEYSTORE=rhdg-https.jks \
  -e JGROUPS_ENCRYPT_SECRET=https-jgroup-secret \
  -e JGROUPS_ENCRYPT_KEYSTORE=jgroups.jceks
```
