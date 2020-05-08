# Installation Steps

# Prerequisites

In order to install USEF framework a number of prerequisite tasks and installation should be done. Specifically the deployment demands the following software to be executed:
-	Java 8 JDK’s (64-bit) either Oracle Java SE Development Kit 8 or OpenJDK JDK 8 
-	Apache Maven with version greater than v3.2.3 
-	JBoss Wildfly version 10.0.0.Final 
DRIMPAC development has only tested with version 10.0.0.Final of JBoss Wildfly as it is the strongly recommended version for USEF framework.
Moreover, Wildfly has a prerequisite to work with “sun.jdk” module. This module needs to be inserted at the dependencies of  “module.xml” file witch is located at  the following path  “${JBOSS_HOME}/modules/system/layers/base/org/picketbox/main”

# Environment variables
The following environment variables need to be defined correctly to be able to run the scripts and installation steps covered in this deliverable:

-	JAVA_HOME – needs to be set to the location of the folder in which the Java JDK 8 will be installed.
-	PATH – must contain a reference to the location of the bin folder in the folder where Apache Maven has been installed, and it must contain the directory “$JAVA_HOME/bin”.
-	LD_LIBRARY_PATH – must contain the path “/usr/local/lib”
-	JBOSS_HOME – needs to be set to the location of the folder in which JBoss Wildfly will be installed.
-	USEF_HOME – needs to be set to the location of the folder where the USEF software bundle is extracted 

# USEF default Database
JBoss Wildfly 10.0.0 Final version is preconfigured to install an H2 Database. Specifically it uses the version 1.3.173. This version contains bugs that prevents the USEF Framework to function properly and it is needed to manually upgraded. The version that DRIMPAC will use for the installation is 1.4.190
The following instruction are given to upgrade the H2 Database:
-	Download the Platform Independent Zip for H2 1.4.190 from http://www.h2database.com/html/download.html
-	Unzip the downloaded file
-	Copy the file h2/bin/h2-1.4.190.jar into ${JBOSS_HOME}/modules/system/layers/base/com/h2database/h2/main
-	Remove h2-1.3.173.jar from ${JBOSS_HOME}/modules/system/layers/base/com/h2database/h2/main
-	Modify the resource root source path in ${JBOSS_HOME}/modules/system/layers/base/com/h2database/h2/main/module.xml into h2-1.4.190.jar.

#	Libsodium

Libsodium is a modern, software library providing nessesarry core operations to build higher-level cryptographic tools used for encryption, decryption, signatures, password hashing. Using Libsodium DRIMPAC will be able to securely transmit and authenticate messages through the USEF framework.
To install the Libsodium software the following steps should be done:
-	sudo apt-get install gcc make
-	Download https://download.libsodium.org/libsodium/releases/libsodium-1.0.10.tar.gz to a new empty directory and start in this directory
-	cd <build_directory>
-	tar xfz libsodium-1.0.10.tar.gz
-	cd libsodium-1.0.10
-	./configure && make
-	sudo make install
-	export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH

# ISC BIND

The installation and usage of ISC Bind  is intended to provide an example on how the DNS will work considering that USEF will instantiate multiple actors in different domain names.
The installation steps of ISC Bind will follow the below sequence:

-	sudo apt-get install bind9
-	cp “usef_ri/usef-environment/config/named.conf” “/etc/bind”
-	cp “usef_ri/usef-environment/config/usef_bind.zone” “/var/cache/bind”
-	sudo service bind9 restart

# Configuration


USEF framework has been implemented to be modular and ease the instantiation and configuration of the platform. In order to achieve that purpose USEF is shipped with predefined template files that will initiliaze the configuration variables for several actors. Those files can be changed to match the needs of the DRIMPAC framework. The initial version of USEF contains configuration variables for 8 USEF participants (3 AGRs, 2 BRPs, 1 CRO, 1 DSO and 1 MDC). 

This configuration entails inside the “usef-environment.yaml” file where the administrator of the system can add / remove USEF participants.

To instantiate DRIMPAC framework at least one AGR one DSO and the CRO are required.

The configuration of the USEF framework is mostly created by the modification of the “usef-environment.yaml” and “standalone-usef.xml” files.


# Configure resolver entries

Each actor in USEF framework will be assigned to a different domain name that will facilitate the framework to transmit messages between the different actors in the corresponding use cases.
For example, if the domains of an Agregator  and a DSO are hosted by the local operating system, then those two domains must resolve to the IP address of the local machine. It is also feasible to specify a separate domain name where Wildfly will be instelled and executed.

If BIND is installed, the IP addresses will be resolved automatically, otherwise to demonstrate the procedure an alternative way to setup the domain names is by applying the following code into the “/etc/hosts” file:

- 127.0.0.1	agr1.miwenergia.com
- 127.0.0.1	cro1.miwenergia.com
- 127.0.0.1	dso1.miwenergia.com
- 127.0.0.1	jboss.miwenergia.com

#	Setting up DRIMPAC server

Drimpac framework transmits sensitive data that need to be handled in certain amount of security level. In order to operate correctly and safely a  self-signed certificate is required. To generate a self-signed certificate, executing the following command in unix shell:
-	openssl genrsa -out key.pem
-	openssl req -new -key key.pem -out csr.pem
-	openssl x509 -req -days 9999 -in csr.pem -signkey key.pem -out cert.pem
-	rm csr.pem

After that, we need to place the generated “key.pem” and “cert.pem” at the root folder of DRIMPAC-DSO server.

The port of Drimpac Server can be set by opending opening .env file and setting:
- DRIMPAC_REST_PORT= "the port of Drimpac server"

-	Open terminal and run “npm install”
*Download Drimpac Server from  https://varlab.iti.gr:9443/H2020-Projects/ICT-IoT-Energy/drimpac/DrimpacDSOui {folder drimpac-rest-api}


# Setting up DSO Graphical User Interface

At DRIMPAC framework, a DSO Graphical User Interface has been implemented to facilitate the users to communicate in demand with Agregators.  DSO Graphical User Interface is able to accomplish all the DSO use cases provided by USEF framework.

To configure and build DSO GUI the following steps are required:

-	Open .env file and set :
window.__env.httpsServer= “the IP of DRIMPAC server”

-	Open terminal and run “npm install”

-	Open terminal and run “ng build --base-href=/ USEF_PATH /” (USEF_PATH needs to be the same as the Domain name of DSO at usef-environment.yaml file)

-	Copy the generated files from DIST folder to the DSO*.war file locaded at usef-environment/nodes/localhost/deployments/.  DSO*.war can be accessed with ark and similar applications.

To test the DSO Graphical User Interface open up a web browser and access the following URL: https://dso1.miwenergia.com:8443/dso1.miwenergiacom_DSO/

*Download DSO Graphical User Interface from  https://varlab.iti.gr:9443/H2020-Projects/ICT-IoT-Energy/drimpac/DrimpacDSOui {folder drimpac}

#	Starting and stopping the DRIMPAC environment

The DRIMPAC environment has been tested on an Ubuntu 18.04 linux machine. USEF installation contains several shell scripts to facilitate the interaction with the platform. Those scripts are located in “usef-environment/bin” directory.

To start the DRIMPAC environment:

-	change directory to “usef-environment/bin”
-	Execute the prepare script (The parameter –skipBuild skips building DRIMPAC, when DRIMPAC has already been built)
-	Run the start-h2-database script.
-	Run the start-usef-environment script
- Open terminal and start Drimpac Server using "npm start"
-	After the start-usef-environment script is executed, you can start sending messages to the participants.

To stop the DRIMPAC environment:
-	change directory to “usef-environment/bin”
-	Stop the DRIMPAC Environment manually by running the stop-usef-environment script
-	Stop the DRIMPAC database by running the stop-h2-database script
-	Run the cleanup script
- Close Drimpac Server using Control+C

