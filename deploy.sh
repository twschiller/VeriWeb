#! /bin/sh

# Rough instructions for running VeriWeb on your service. More of a README, don't actually
# run this script.

# Environment Setup

sudo apt-get update
sudo apt-get install default-jdk
sudo apt-get install tomcat7 tomcat7-admin
sudo apt-get install git
sudo apt-get install maven

# Configure Tomcat: modify tomcat-users.xml in /etc/tomcat7/tomcat-users.xml
# <!-- Lazy configuration. Tomcat instruction indicate that user should have both gui and script role -->
# <role rolename="tomcat" />
# <role rolename="manager-gui" />
# <role rolename="manager-script" />
# <role rolename="admin-gui" />
# <user username="tomcat" password="tomcatuser"  roles="tomcat,manager-gui,admin-gui,manager-script" />

# Set up log4j logging
# XXX: shouldn't need to do this, as the application has log4j-1.2.16 in the WEB-INF
# XXX: perhaps I need to add a log4j.properties file to the WEB-INF directory too?
# https://tomcat.apache.org/tomcat-7.0-doc/logging.html#Using_Log4j

sudo service tomcat7 restart

# Configure Maven @ ${user.home}/.m2/settings.xml
# <settings>
# <servers>
# <server>
#   <id>tomcat-localhost</id>
#   <username>tomcat</username>
#   <password>tomcatuser</password>
# </server>
# </servers>
# </settings>

# Install VeriWeb

git clone https://github.com/twschiller/VeriWeb.git
cd VeriWeb

wget https://dl.dropboxusercontent.com/u/861293/veriweb/lib/jml-release.jar
mvn install:install-file -Dfile=jml-release.jar -DgroupId=org.jmlspecs -DartifactId=jml-release -Dversion=1.38 -Dpackaging=jar
wget https://dl.dropboxusercontent.com/u/861293/veriweb/lib/daikon.jar
mvn install:install-file -Dfile=daikon.jar -DgroupId=daikon -DartifactId=daikon -Dversion=4.6.4 -Dpackaging=jar
wget https://dl.dropboxusercontent.com/u/861293/veriweb/lib/mobius.escjava2.esctools_2.0.22.jar
mvn install:install-file -Dfile=mobius.escjava2.esctools_2.0.22.jar -DgroupId=mobius.escjava2 -DartifactId=esctools2 -Dversion=2.0.22 -Dpackaging=jar

export MAVEN_OPTS=-Xmx1024m
mvn clean install

# Install ESCJava2
cd $HOME
wget http://kindsoftware.com/products/opensource/archives/ESCJava-2.0.5-04-11-08-binary.tgz
mkdir esctools
tar zxf ESCJava-2.0.5-04-11-08-binary.tgz esctools

# Install JAVA SDK 1.4
sudo apt-get install gcc-multilib # required to install 32-bit VM on 64-bit machine
wget https://dl.dropboxusercontent.com/u/861293/veriweb/bin/j2sdk-1_4_2_19-linux-i586.bin

# Setup VeriWeb Environment
export VERIASA_PROJS=$HOME/VeriWeb/projects
export VERIASA_LOG_DIR=$HOME/VeriWeb/log
export ESCTOOLS_ROOT=$HOME/esctools/
export SIMPLIFY=Simplify-1.5.4.linux
export JDK14=$HOME/j2sdk1.4.2_19

# Deploy VeriWeb verification server
nohup sh VeriWeb/src/SpecService/target/appassembler/bin/veriasa.sh &

# Deploy VeriWeb servlet
cd ~/VeriWeb/src/VeriWeb
mvn tomcat7:redeploy
