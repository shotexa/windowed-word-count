# Windowed word count


### Ziverge "windowed-word-count" coding challenge

*Note*: This SBT project is generated from my personal [gitter8 template](https://github.com/shotexa/scala-seed.g8) which I use for all of my projects. There is a lot of boilerplate code designed to adjust the development environment to my personal taste and is not specific for this coding challenge. Files and folders you should look at are:

* `src/main/scala/com/ziverge/**`
* `src/test/scala/com/ziverge/**`
* `dependency-graph.sbt`

### Run the project
In order to start up the project, you will need following dependencies installed on the system:

 * java 8 or java 11
 * sbt
 * /bin/sh 
 
 **the binary file has to be placed in the project root (on the same level as `build.sbt` file) and named "blackbox"**

`cd` into project root and run `sbt run`, this will start up the server, and start a blackbox binary as a subprocess. The server will expose 2 endpoints:

* http://localhost:8080/word-count/current
* http://localhost:8080/word-count/history (bonus)

When running the binary file manually in my terminal, it looks like the events always arrive in order and data is always one world long, however, for the sake of the assignment I've assumed that events might not arrive in order (as it always happens) and data can be a more then one word, and implemented the project accordingly.

For windowing, I'm using 5 second interval tumbling window with 10 second watermark to handle events that are coming late. I'm deriving event time from the timestamp of the event. Because of this reason you will only be able to view result from any endpoint 10 seconds after starting up the project, when the first window closes. I'm assembling the windows and storing them in memory, you can view the list of windows that have been assembled by the order of their arrival on the endpoint http://localhost:8080/word-count/history. it will look like this:

![History](https://i.imgur.com/EQPhCib.png)

You can view the world count grouped by an event type on the endpoint http://localhost:8080/word-count/current. it will look like this:

![Word count](https://i.imgur.com/CJe1cez.png)

### Run tests

run `sbt test`



*Note*: The project was tested on macos with java 11 and java 8