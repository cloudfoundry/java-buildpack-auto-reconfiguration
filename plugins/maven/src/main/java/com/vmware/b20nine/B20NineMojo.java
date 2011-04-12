package com.vmware.b20nine;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @goal deploy-grails
 * @execute phase=package
 */
@SuppressWarnings({"JavaDoc"})
public class B20NineMojo extends AbstractMojo {

    /**
     * The location of the war file.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}.war"
     * @required
     */
    private File webApp;

    /**
     * The base deployment URI.
     *
     * @parameter
     * @required
     */
    private String baseUri;


    /**
     * The application name.
     *
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String name;

    /**
     * The cloud controller address.
     *
     * @parameter default-value="http://localhost:8080"
     * @required
     */
    private String cloudcontroller;


    /**
     * Number of instances to start.
     *
     * @parameter default-value="1"
     * @required
     */
    private int instances;

    /**
     * Username for accessing the cloud controller.
     *
     * @parameter
     * @required
     */
    private String user;

    /**
     * Password for accessing the cloud controller.
     *
     * @parameter
     * @required
     */
    private String password;

    private String token;

    public B20NineMojo() {
        client = new DefaultHttpClient();
    }

    public void execute() throws MojoExecutionException {
        getLog().info(String.format("Deploying \"%s\"...", name));

        try {
            login();
            JsonNode droplet = getExistingDroplet();
            if (droplet != null) {
                String framework = droplet.get("framework").getTextValue();
                if (!GRAILS_FRAMEWORK.equals(framework)) {
                    throw new MojoExecutionException(String.format(
                            "Can't update this application since it's not using the Grails framework: %s", framework));
                }
                getLog().info("Updating existing application");
                uploadApplication();
                getLog().info("Restarting application.");
                updateState(droplet, "STOPPED");
                updateState(droplet, "STARTED");
                getLog().info(String.format("Updated %s", name));
            } else {
                createApplication();
                provisionDatabase();
                uploadApplication();
                updateState(getExistingDroplet(), "STARTED");
                getLog().info(String.format("Deployed %s", name));
            }
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Error deploying \"%s\" to: %s", name, baseUri), e);
        }
    }

    private JsonNode getExistingDroplet() throws IOException, MojoExecutionException {
        HttpGet request = new HttpGet(cloudcontroller + "/droplets/" + name);
        request.addHeader(AUTHORIZATION_HEADER, token);
        HttpResponse response = client.execute(request);
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode == HttpStatus.SC_OK) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonParser parser = jsonFactory.createJsonParser(response.getEntity().getContent());
            JsonNode droplet = objectMapper.readTree(parser);
            consumeContent(response);
            return droplet;
        } else {
            consumeContent(response);
            if (responseCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            } else {
                throw new MojoExecutionException("Could not look up existing application status.");
            }
        }
    }

    private void uploadApplication() throws MojoExecutionException, IOException {
        HttpPut request = new HttpPut(cloudcontroller + "/droplets/" + name + "/application");
        request.addHeader(AUTHORIZATION_HEADER, token);

        MultipartEntity entity = new MultipartEntity(HttpMultipartMode.STRICT);
        entity.addPart("application", new FileBody(webApp, "application/x-zip"));        
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        consumeContent(response);
        if (response.getStatusLine().getStatusCode() < 400) {
            getLog().info(String.format("Uploaded %s bits", name));
        } else {
            throw new MojoExecutionException(String.format("Error deploying \"%s\" to: %s", name, baseUri));
        }
    }

    private void updateState(JsonNode droplet, String state) throws MojoExecutionException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ((ObjectNode) droplet).put("state", state);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        objectMapper.writeValue(out, droplet);

        HttpPut request = new HttpPut(cloudcontroller + "/droplets/" + name);
        request.addHeader(AUTHORIZATION_HEADER, token);
        ByteArrayEntity entity = new ByteArrayEntity(out.toByteArray());
        entity.setContentType("application/json");
        request.setEntity(entity);

        HttpResponse response = client.execute(request);
        consumeContent(response);
        if (response.getStatusLine().getStatusCode() >= 400) {
            throw new MojoExecutionException("Could not update application state.");
        }
    }

    private void login() throws IOException, MojoExecutionException {
        HttpPost request = new HttpPost(cloudcontroller + "/login");

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
        gen.writeStartObject();
        gen.writeStringField(EMAIL_FIELD, user);
        gen.writeStringField(PASSWORD_FIELD, password);
        gen.writeEndObject();
        gen.close();

        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(writer.toString()));

        HttpResponse response = client.execute(request);
        if (response.getStatusLine().getStatusCode() < 400) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonParser parser = jsonFactory.createJsonParser(response.getEntity().getContent());
            JsonNode tokenJson = objectMapper.readTree(parser);
            token = tokenJson.get("token").getTextValue();
            consumeContent(response);
            getLog().info(String.format("Logged in as \"%s\"", user));
        } else {
            consumeContent(response);
            throw new MojoExecutionException(String.format("Error logging in as \"%s\"", user));
        }
    }

    private void createApplication() throws IOException, MojoExecutionException {
        HttpPost request = new HttpPost(cloudcontroller + "/droplets");
        request.addHeader(AUTHORIZATION_HEADER, token);

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
        gen.writeStartObject();
        gen.writeStringField("name", name);
        gen.writeArrayFieldStart("uris");
        gen.writeString(baseUri);
        gen.writeEndArray();
        gen.writeStringField("framework", GRAILS_FRAMEWORK);
        gen.writeNumberField("instances", instances);
        gen.writeEndObject();
        gen.close();

        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(writer.toString()));

        HttpResponse response = client.execute(request);
        consumeContent(response);
        if (response.getStatusLine().getStatusCode() < 400) {
            getLog().info(String.format("Created application \"%s\"", name));
        } else {
            throw new MojoExecutionException(String.format("Error creating \"%s\"", name));
        }
    }

    private void provisionDatabase() throws IOException, MojoExecutionException {
        HttpPost request = new HttpPost(String.format("%s/droplets/%s/services", cloudcontroller, name));
        request.addHeader(AUTHORIZATION_HEADER, token);

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
        gen.writeStartObject();
        gen.writeStringField("type", "database");
        gen.writeStringField("vendor", "mysql");
        gen.writeStringField("version", "5");
        gen.writeStringField("tier", "small");
        gen.writeEndObject();
        gen.close();

        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(writer.toString()));

        HttpResponse response = client.execute(request);
        consumeContent(response);
        if (response.getStatusLine().getStatusCode() < 400) {
            getLog().info("Provisioned database");
        } else {
            throw new MojoExecutionException("Error provisioning database");
        }
    }

    private void consumeContent(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity.consumeContent();
        }
    }

    private final HttpClient client;

    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final String GRAILS_FRAMEWORK = "http://www.grails.org/1.1.1";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String EMAIL_FIELD = "email";
    private static final String PASSWORD_FIELD = "password";    
}
