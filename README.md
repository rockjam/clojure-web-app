## Clojure web app

Application to learn more about different Clojure libraries for building web services and apps.

Libraries used:

* ring with Jetty as a server
* compojure for routing
* next.jdbc for database access with sqlite database
* hiccup to render HTML

### Build and run

Application is packaged as an Uber-JAR, and as a native binary using GraalVM Native Image

Build Uber-JAR: `clojure -T:build uber`, run: `java -jar target/app.jar db/prod`

Build native binary: `clojure -T:build uber && clojure -T:build native-image`, run: `target/app db/prod`  
