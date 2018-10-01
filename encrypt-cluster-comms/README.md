# Encrypting Cluster Communication
Create OpenShift secrets to encrypt client to server and server to server traffic.

## Certificates and Keystores
Data Grid can use keystores to encrypt network traffic.

You can use OpenSSL and the Java keytool to generate HTTPS and JGroups keystores.

### HTTPS Keystore
The HTTPS keystore (`.jks`) contains a username and password that clients use to authenticate with the server. The HTTPS keystore should also contain a signed TLS certificate that it presents to clients.

**IMPORTANT:** In production environments, you should submit a certificate signing request (CSR) to a verified certificate authority (CA) to sign the TLS certificate.

**NOTE:**
When you generate a TLS certificate for the HTTPS keystore, you should specify the domain name for the deployment. For example:
`-dname "CN=rhdg-https-demo.openshift32.example.com"`.

Configure your deployment to use the HTTPS keystore with the following environment variables:

* `HOSTNAME_HTTP` specifies the HTTP service route for the deployment.
* `HOSTNAME_HTTPS` specifies the HTTPS service route for the deployment.
* `HTTPS_KEYSTORE` specifies the name of a keystore that contains an TLS certificate secures communication with the Data Grid application.
* `HTTPS_SECRET` specifies the name of the OpenShift secret that contains the keystore.
* `HTTPS_NAME` sets the name that is associated with the TLS certificate in the keystore.
* `HTTPS_PASSWORD` sets the password for the TLS certificate in the keystore.

### JGroups Keystore
Data Grid uses JGroups technology to encrypt traffic between clustered servers. The JGroups keystore (`.jceks`) contains a key and password that clustered servers can use to encrypt traffic.

Configure your deployment to use the JGroups keystore with the following environment variables:

* `JGROUPS_ENCRYPT_KEYSTORE` specifes the name of a keystore that contains a secret key for encrypting cluster traffic.
* `JGROUPS_ENCRYPT_SECRET` specifies the name of the OpenShift secret that contains the keystore.
* `JGROUPS_ENCRYPT_PASSWORD` sets the password for the keystore.

## Creating OpenShift Secrets for Encrypted Traffic
After you create an HTTPS and JGroups keystore, you must make them available in OpenShift as secrets.

For example, you create an HTTPS keystore named *rhdg-https.jks* and a JGroups keystore named *jgroups.jceks*.

You can create secrets with these keystores as follows:

1. Log in as the developer user.
  ```bash
  $ oc login -u developer
  ```

2. Create a secret for the HTTPS keystore.
  ```bash
  $ oc create secret generic rhdg-https-secret --from-file=rhdg-https.jks
  ```

3. Create a secret for the JGroups keystore.
  ```bash
  $ oc create secret generic https-jgroup-secret --from-file=jgroups.jceks
  ```

4. Link secrets to the default service account.
  ```bash
  $ oc secrets link default https-jgroup-secret rhdg-https-secret
  ```

You can now add the secrets to deployments. For example, to create a new deployment from the `datagrid72-https` template, do the following:

```bash
$ oc new-app --template=datagrid72-https --name=rhdg-https \
  -e HTTPS_SECRET=rhdg-https-secret \
  -e HTTPS_KEYSTORE=rhdg-https.jks \
  -e JGROUPS_ENCRYPT_SECRET=https-jgroup-secret \
  -e JGROUPS_ENCRYPT_KEYSTORE=jgroups.jceks
```

## Trying Example Secrets
Data Grid provides example HTTPS and JGroups keystores that you can import as OpenShift secrets for evaluation purposes.

Do the following to import and create the example secrets:

```bash
$ oc login -u system:admin

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.1/templates/datagrid72-image-stream.json

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-openshift/application-templates/master/secrets/datagrid-app-secret.json

$ oc create \
  -n openshift \
  -f https://raw.githubusercontent.com/jboss-container-images/jboss-datagrid-7-openshift-image/1.1/templates/datagrid72-https.json
```

You can now log in as the `developer` user and create a deployment with the `datagrid72-https` template that uses the example secrets.
