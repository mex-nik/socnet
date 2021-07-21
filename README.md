#**SocNet**
by Mladen Nikolic, [mladen.nikolic.mex@gmail.com](mailto:mladen.nikolic.mex@gmail.com)

##General information
**SocNet** is an application with some features of a social network site.

As in every social network, there are users of this application.
In fact, there is a set of thousand users that will be loaded when the application starts.
Users can have admin or regular roles and different permissions based on the role.
All users are able to publish posts and chat with other users,
change personal data and view others' data and posts.
Admin users are able to add new users, change user's data and privileges and remove users or their posts.
A regular user can change general info about himself. Feel free to try using **SocNet** and investigate its possibilities.

The general idea when started work on **SocNet** was not to create a production grade application
but to investigate and build an application using some interesting solutions of **Spring Boot**.
Hope that this will explain some strange decisions in code and application design.


**SocNet** is created using **Spring Boot** implementations:

-   MVC
-   Web
-   Thymeleaf
-   STOMP web sockets
-   Security
-   JPA
-   Data REST
-   H2 Database
-   Kafka
-   Tests

Other technologies used:

-   Kafka
-   Docker
-   Docker maven plugin
-   Lombok
-   Mockito
-   HTML, CSS

##Starting **SocNet**

##IDE
Before starting the application in IDE, prepare the environment by starting Kafka and Zookeeper docker containers from the command line:
``docker-compose -f socnet-kafka.yml -p sn up -d``

To stop Kafka and Zookeeper docker containers:
``docker-compose -f socnet-kafka.yml -p sn down``

##Command line and Docker 

It is possible to create a docker container with the application by running:
``mvn package docker:build -Dapp-profile=default``

After creating the docker container, run the application with the command:

``docker-compose -f socnet.yml -p sn up -d``

Stop with:

``docker-compose -f socnet.yml -p sn down``

##Accessing **SocNet**

After setting up environment and starting **SocNet**, it is available at [http://localhost:8080/](http://localhost:8080/).

Login is possible with following credentials:

- As admin user:
    - email: admin@admin.com
    - password: admin
- As regular user:
    - email: regular@regular.com
    - password: regular