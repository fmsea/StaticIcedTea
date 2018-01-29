#! /bin/bash
locpath="$1"
filename="$2"
#c1 - pseudo, c2 -coditional or f - full
type="$3"
echo $type
writeToFile="$4"

pathToBuild="/Users/elenasherman/Documents/z3Java/z3/build/"
pathToZ3=${pathToBuild}com.microsoft.z3.jar
export DYLD_LIBRARY_PATH=$pathToBuild:.
echo $DYLD_LIBRARY_PATH

while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
if [ "$type" != "f" ]; then 
   pathfile=$locpath/conditions/paths/${class}_${method}.txt
   echo ${pathfile}
  driver="driver.StartConditionalValue"
  if [ "$type" == "c1" ]; then
   driver="driver.StartPseudoConditionalValue"
  fi
  if [ -f $pathfile ]; then
     while read -r path
     do
       echo $class $method $path
       java  -cp .:./bin/:./libs/soot-trunk.jar:$pathToZ3:./libs/antlr-4.1-complete.jar  $driver $locpath/resultsVA/ $class $method dom4 $path $writeToFile
     done < $pathfile
  else 
     echo 'File $pathfile does not exists.'
  fi
else
 # run regular analysis with all paths - idex 0 means all paths
 echo $class $method 'full'
     java -cp .:./bin/:./libs/soot-trunk.jar:$pathToZ3:./libs/antlr-4.1-complete.jar  driver.StartValue $locpath/resultsVA/ $class $method dom4 $writeToFile
fi
done < $filename
