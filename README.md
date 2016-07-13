# Java ClamAV Client Library [![Build Status](https://travis-ci.org/cdarras/clamav-client.svg?branch=master)](https://travis-ci.org/cdarras/clamav-client)

A simple yet efficient Java client library for the [ClamAV antivirus](https://www.clamav.net/) daemon.

## Pre-requisites

This library requires a JDK version 8.

## Installing

### With Maven
Add this dependency to the `<dependencies>` section of your `pom.xml` file:

```xml
<dependency>
    <groupId>xyz.capybara</groupId>
    <artifactId>clamav-client</artifactId>
    <version>1.0.3</version>
</dependency>
```

### With Gradle
Add this dependency to the `dependencies` section of your `build.gradle` file:

```gradle
compile 'xyz.capybara:clamav-client:1.0.3'
```

### Manually
Alternatively, you can download the jar file of this library directly from the Maven Central Repository website and add it to the classpath of your application: [Download page](http://search.maven.org/#search|gav|1|g%3A%22xyz.capybara%22%20AND%20a%3A%22clamav-client%22).

## Usage

After the library has been added to your build, start by creating an instance:

```java
ClamavClient client = new ClamavClient("localhost");
```

By default, the client will try to connect to the port `3310` which is the default ClamAV daemon port.

If your ClamAV daemon listens to another port, you can indicate it with:

```java
ClamavClient client = new ClamavClient("localhost", 3311);
```

**Be careful** if you intend to use the functionality of scan of a file/directory on the server filesystem and if the ClamAV daemon is running on an OS having a different path separator than the OS on which your Java application is running.
(for example, if your Java application is running on a Windows platform but the ClamAV daemon is running on a remote UNIX platform)

You will then have to explicitly indicate the target server platform to the client library at instantiation:
```java
ClamavClient client = new ClamavClient("localhost", Platform.UNIX);

// Or with an alternate port number:
ClamavClient client = new ClamavClient("localhost", 3311, Platform.UNIX);
```

By default, the chosen file separator will be the one of the platform your Java application is running on.

### Commands

#### Scan commands

```java
ScanResult scan(InputStream inputStream)
```

Scans an `InputStream` and sends a response as soon as a virus has been found.

```java
ScanResult scan(Path path)
```

Scans a file/directory on the filesystem of the ClamAV daemon and sends a response as soon as a virus has been found.

```java
ScanResult scan(Path path, boolean continueScan)
```

Scans a file/directory on the filesystem of the ClamAV daemon and may continue the scan to the end even if a virus has been found, depending on the `continueScan` argument.

```java
ScanResult parallelScan(Path path)
```

Scans a file/directory on the filesystem of the ClamAV daemon and will continue the scan to the end even if a virus has been found.
This method may improve performances on SMP systems by performing a multi-threaded scan.

#### Scan result

The `ScanResult` object returned by the scan commands is a holder for two informations:

1. The status of the scan: `OK` or `VIRUS_FOUND`
2. Information about the found infected files as a map filled as following:
  - Key: infected file path
  - Value: list of viruses found in the file

#### Admin commands

```java
void reloadVirusDatabases()
```

Triggers the virus databases reloading by the ClamAV daemon.

```java
void shutdownServer()
```

Immediately shutdowns the ClamAV daemon.

#### Other commands

```java
void ping()
```

Pings the ClamAV daemon. If a correct response has been received, the method simply returns. Otherwise, a `ClamavException` exception is thrown.

```java
String version()
```

Requests the version of the ClamAV daemon.

```java
String stats()
```

Requests stats from the ClamAV daemon.

## Building from sources

To build this library from its sources, an installation of Maven is required.
Clone this repository and launch its build with the Maven command:

```maven
mvn clean package
```

If the build is successful, the jar file of the library will be found into the `target` directory.

## Contributing

Feel free to fork this repository and submit pull requests :)

You can also submit issues in case of bugs or feature requests.

## Licensing

The code in this project is licensed under MIT license.
The content of the license can be found in the `LICENSE` file under the root of this repository.
