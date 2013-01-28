#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
DIR="$( dirname "$SOURCE" )"
while [ -h "$SOURCE" ]
do 
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
  DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd )"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

#java -cp $DIR/../lib/json.jar:$DIR/../lib/jsoup-1.7.1.jar:$DIR/../lib/commons-codec-1.3.jar:$DIR/../lib/commons-httpclient-3.1.jar:$DIR/../lib/commons-logging-1.1.1.jar:$DIR/../lib/htmlcleaner1_55.jar:$DIR/../lib/jruby-complete-1.6.7.jar:$DIR/../lib/yacli.jar:$DIR/../bin es.uvigo.ei.sing.jarvest.JarvestApp "$@"
java -jar $DIR/../target/jarvest-*-with-dependencies.jar "$@"

