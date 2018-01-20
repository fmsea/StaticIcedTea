#! /bin/bash
export DYLD_LIBRARY_PATH=~/Documents/z3Java/z3/build/:.
echo $DYLD_LIBRARY_PATH
filename="$1"
#c1 - pseudo, c2 -coditional or f - full
type="$2"
writeToFile="$3"
echo $type 
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
if [ "$type" != "f" ]; then 
   pathfile=./ExperimentDataConditional/conditions/paths/${class}_${method}.txt
   echo ${pathfile}
  driver="driver.StartConditionalValue"
  if [ "$type" == "c1" ]; then
   driver="driver.StartPseudoConditionalValue"
  fi
  if [ -f $pathfile ]; then
     while read -r path
     do
       echo $class $method $path
       java  -cp .:./bin/:./libs/soot-trunk.jar:/Users/elenasherman/Documents/z3Java/z3/build/com.microsoft.z3.jar:./libs/antlr-4.1-complete.jar  $driver $class $method dom4 $path $writeToFile
     done < $pathfile
  else 
     echo 'File $pathfile does not exists.'
  fi
else
 # run regular analysis with all paths - idex 0 means all paths
 echo $class $method 'full'
     java -cp .:./bin/:./libs/soot-trunk.jar:/Users/elenasherman/Documents/z3Java/z3/build/com.microsoft.z3.jar:./libs/antlr-4.1-complete.jar  driver.StartValue $class $method dom4 $writeToFile
fi
done < $filename
