# goocle

A Clojure library designed to ... well, that part is up to you.

## Usage

FIXME

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
## Prerequisites

If building on a mac, you need to have gsed installed (perhaps using brew) and being used
instead of the standard osx sed, e.g. by putting it into your path before sed.

## Building google-cloud-java

google-cloud-java is built without named parameters being stored. This is not very useful
for this library as it produces function that have parameters names `arg0` `arg1` etc...

But there is a way to get the google cloud java libraries to be compiled with named parameters
included.

Firstly, clone the google-cloud-java library from `git@github.com:GoogleCloudPlatform/google-cloud-java.git`.

Then checkout the version from which you wish to generate `goocle`.

Run the command `./utilities/update_pomversion.sh <version>-WithParameters`

This will change all the version numbers to something like `0.7.0-WithParameters`.

Install the code to your local repository using:

```
mvn clean install -EskipTests
```

If you're happy to wait longer remove `-EskipTests`.

Build `goocle` with the newly installed version of `google-cloud-java` and named parameters will
be present.
