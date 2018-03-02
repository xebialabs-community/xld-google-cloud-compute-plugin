package org.xebialabs.community.googlecloud;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.InstanceList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Command-line sample to demo listing Google Compute Engine instances using Java and the Google
 * Compute Engine API
 *
 * @author Jonathan Simon
 */
public class ComputeEngineSample {

    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "MyCompany-ProductName/1.0";

    /**
     * Set projectId to your Project ID from Overview pane in the APIs console
     */
    private static final String projectId = "just-terminus-194507";

    /**
     * Set Compute Engine zone
     */
    private static final String zoneName = "us-central1-a";



    /**
     * Directory to store user credentials.
     */
    private static final java.io.File DATA_STORE_DIR =
        new java.io.File(System.getProperty("user.home"), ".store/compute_engine_sample");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * OAuth 2.0 scopes
     */
    private static final List<String> SCOPES = Arrays.asList(ComputeScopes.COMPUTE);

    public static void main(String[] args) {
        // Start Authorization process
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            // Authorization
            Credential credential = authorize();

            // Create compute engine object for listing instances
            Compute compute = new Compute.Builder(
                httpTransport, JSON_FACTORY, null).setApplicationName(APPLICATION_NAME)
                .setHttpRequestInitializer(credential).build();

            // List out instances
            printInstances(compute, projectId);
            // Success!
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(1);
    }

    /**
     * Authorizes the installed application to access user's protected data.
     */
    private static Credential authorize() throws Exception {


        if (false ) {
            // initialize client secrets object
            GoogleClientSecrets clientSecrets;
            // load client secrets
            //String json_file_path = "/Users/bmoussaud/.ssh/MyFirstProject-3606a89398e9.json";
            String json_file_path = "/Users/bmoussaud/.ssh/gcloud.json";
            // load client secrets
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(
                    new FileInputStream(json_file_path)));

            //clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(
            //    ComputeEngineSample.class.getResourceAsStream("/client_secrets.json")));
            if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
                System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
                    + "into compute-engine-cmdline-sample/src/main/resources/client_secrets.json");
                System.exit(1);
            }
            // set up authorization code flow
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(dataStoreFactory)
                .build();
            // authorize
            return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        }
        String json_file_path_raw = "/Users/bmoussaud/.ssh/MyFirstProject-3606a89398e9.json";
        if (false) {
            GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(json_file_path_raw));
        }

        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream(json_file_path_raw));
        return credential.createScoped(SCOPES);
    }

    /**
     * Print available machine instances.
     *
     * @param compute   The main API access point
     * @param projectId The project ID.
     */
    public static void printInstances(Compute compute, String projectId) throws IOException {
        System.out.println("================== Listing Compute Engine Instances ==================");
        Compute.Instances.List instances = compute.instances().list(projectId, zoneName);
        InstanceList list = instances.execute();
        if (list.getItems() == null) {
            System.out.println("No instances found. Sign in to the Google APIs Console and create "
                + "an instance at: code.google.com/apis/console");
        } else {
            for (Instance instance : list.getItems()) {
                System.out.println(instance.toPrettyString());
            }
        }
    }
}