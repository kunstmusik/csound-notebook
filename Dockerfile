FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/csound-notebook.jar /csound-notebook/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/csound-notebook/app.jar"]
