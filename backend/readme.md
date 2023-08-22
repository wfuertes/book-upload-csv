## Setup local environment


### Local Infra

```bash
cd local-infra
docker-compose up
```

### MySQL
1. Connect to MySQL and create the database `bookstore`
2. and create the tables use the script `resources/db/tables.sql`

```bash
docker pull mysql
docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:latest
```

### Keycloack
https://www.keycloak.org/getting-started/getting-started-docker

```bash
docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:22.0.1 start-dev
```
It will be required to create the realm **NeverCoded** (setup to allow user register and forgot password), 
and import the `resources/book-upload-csv.json` under the clients
in order to create the keycloak client used for oauth in our FE.

### Graylog
Documentation: https://go2docs.graylog.org/5-1/downloading_and_installing_graylog/docker_installation.htm

Sending logs to Graylog involves two primary configuration steps:

1. Configuring an input in Graylog where the logs should be sent.
2. Configuring your application or server to forward logs to that input.

Let's walk through both steps:

### 1. Configuring an Input in Graylog:

1. **Log in to Graylog**: Access the Graylog web interface, typically at `http://localhost:9000/`.

2. **Create an Input**:
    - Go to `System` -> `Inputs`.
    - Click on `Select input` drop-down and select `GELF UDP` (for sending logs in the GELF format over UDP). There are many other input types, and the right one depends on your specific requirements.
    - Click on `Launch new input`.
    - Fill in the necessary details:
        - **Title**: Name your input.
        - **Bind Address**: `0.0.0.0` (to listen on all interfaces) or the specific IP you want Graylog to listen on.
        - **Port**: Choose an unused port like `12201` (this is the default for GELF).

3. Click `Save`.

### 2. Configuring Your Application or Server:

The method to forward logs to Graylog depends on your application or server. I'll provide a general method using the `logger` command on a Unix-based system and a method for a Java application:

- **Using `logger` Command**:
   ```bash
   echo "Hello Graylog!" | logger -n localhost -P 12201 -T -d
   ```

- **Java Application with Logback**:
  If you're using Logback in a Java application, you can use the `logback-gelf` extension to send logs to Graylog:
    1. Add the `logback-gelf` dependency to your project.
    2. Configure Logback with a GELF appender.
    3. Logs from your Java application will then be forwarded to the Graylog input.

Another common method involves using `Filebeat` or `rsyslog` to forward logs from various systems to Graylog.

Remember, the specifics can vary based on the source of your logs and the technology stack you're using. If you provide more details about where your logs are originating, I can give a more tailored instruction.

Of course! If you want to send logs from your Java application to Graylog, one of the common methods is to use a logging library like Logback or Log4j2 with a GELF appender.

Here's a step-by-step guide on how to set this up using Logback:

### 1. Add Dependencies:

First, you need to add the necessary dependencies to your project. For a Maven project, you can add:

```xml
<!-- Logback Classic -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.6</version>
</dependency>

<!-- Logback GELF Appender -->
<dependency>
    <groupId>de.siegmar.logbackgelf</groupId>
    <artifactId>logback-gelf</artifactId>
    <version>2.2.0</version>
</dependency>
```

(Note: Ensure you check for the latest version of these dependencies.)

### 2. Modify `logback.xml`:

Next, you need to modify your `logback.xml` configuration to use the GELF appender:

```xml
<configuration>
    <!-- GELF Appender Configuration -->
    <appender name="GELF" class="de.siegmar.logbackgelf.GelfUdpAppender">
        <graylogHost>localhost</graylogHost>
        <graylogPort>12201</graylogPort>
        <maxChunkSize>508</maxChunkSize>
        <useCompression>true</useCompression>
        <encoder class="de.siegmar.logbackgelf.GelfEncoder">
            <originHost>localhost</originHost>
            <includeRawMessage>false</includeRawMessage>
            <includeMarker>true</includeMarker>
            <includeMdcData>true</includeMdcData>
            <includeCallerData>false</includeCallerData>
            <includeRootCauseData>false</includeRootCauseData>
            <includeLevelName>true</includeLevelName>
            <shortPatternLayout class="ch.qos.logback.classic.PatternLayout">
                <pattern>%m%nopex</pattern>
            </shortPatternLayout>
            <fullPatternLayout class="ch.qos.logback.classic.PatternLayout">
                <pattern>%m</pattern>
            </fullPatternLayout>
        </encoder>
    </appender>

    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <!-- Define colors: error in red, warn in yellow, debug in cyan, all others in default color -->
            <pattern>%highlight(%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n)</pattern>
        </encoder>
    </appender>

    <root level="debug">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="GELF" />
    </root>
</configuration>
```

Make sure to replace `localhost` in the `<graylogHost>` tag with the address of your Graylog server if it's not running on the same machine. The `<graylogPort>` should match the port you configured for your GELF UDP input in Graylog.

### 3. Java Code:

With everything set up, any log statements in your Java application will now be sent to Graylog. Here's a simple example:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyApp {
    private static final Logger logger = LoggerFactory.getLogger(MyApp.class);

    public static void main(String[] args) {
        logger.info("This log will be sent to Graylog!");
    }
}
```

Run your application, and you should see the log message appear in Graylog.
That's it! This will send your logs from the Java application to the Graylog server. Remember to handle exceptions and failures appropriately in a production scenario.