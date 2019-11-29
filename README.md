# InformationRetrieval
## Pre-requisites
The pre-requisites required to run this project are:

* Java 8
* Maven 3.6.2

## Build and Run
To build and run the spring program, execute the following command:
```java
mvn spring-boot:run
```
This java command will start the program and open tomcat to listen on port 8080. 

To view the webpage, visit ` http://localhost:8080 ` in your browser.

## Download

A link to where maven can be downloaded can be found at:  
https://maven.apache.org/download.cgi

## Code Organization  

The code is organized into 4 modules  
* Indexer - The Java code to build the index files  
* Parser - The Java code to turn the HTML file into documents  
* Query - The controller for the web server  
* Retrieval - Where the IR retrieval algorithms are stored  

### Resources:  
The front-end UI code is stored under `src/resources`, which is then broken into 4 directories  
* CSS - Where all the styling code is stored
* IMG - Where the images are stored
* JS - Where the Javascript code is stored
* Templates - Where the HTML files that get rendered are stored