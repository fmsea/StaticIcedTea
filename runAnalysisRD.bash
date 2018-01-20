#! /bin/bash
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
  driver="driver.StartConditionalReachingDefinitions"
  if [ "$type" == "c1" ]; then
   driver='driver.StartPseudoConditionalReachingDefinitions'
  fi
  if [ -f $pathfile ]; then
     while read -r path
     do
       echo $class $method $path
       java -cp .:./bin/:./libs/soot-trunk.jar:  $driver $class $method $path $writeToFile
     done < $pathfile
  else 
     echo 'File $pathfile does not exists.'
  fi
else
 # run regular analysis with all paths - idex 0 means all paths
 echo $class $method 'full'
     java -cp .:./bin/:./libs/soot-trunk.jar:  driver.StartReachingDefinitions $class $method $writeToFile
fi
done < $filename
