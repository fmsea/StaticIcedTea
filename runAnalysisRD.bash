#! /bin/bash
# path to where write the output
# make it it has data and invariants folders inside it
# and also contains the conditions and paths
loc="$1"
#the file containing classes and methods
filename="$2"
#c1 - pseudo, c2 -conditional or f - full
type="$3"
writeToFile="$4"
echo $type 
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
if [ "$type" != "f" ]; then 
   pathfile=./${loc}/conditions/paths/${class}_${method}.txt
   echo ${pathfile}
  driver="driver.StartConditionalReachingDefinitions"
  if [ "$type" == "c1" ]; then
   driver='driver.StartPseudoConditionalReachingDefinitions'
  fi
  if [ -f $pathfile ]; then
     while read -r path
     do
       echo ${loc}/resultsRD/ $class $method $path $wrtieToFile
       java -cp .:./bin/:./libs/soot-trunk.jar:  $driver ${loc}/resultsRD/ $class $method $path $writeToFile
     done < $pathfile
  else 
     echo 'File $pathfile does not exists.'
  fi
else
 # run regular analysis with all paths - idex 0 means all paths
 echo $class $method 'full'
     java -cp .:./bin/:./libs/soot-trunk.jar:  driver.StartReachingDefinitions ${loc}/resultsRD/ $class $method $writeToFile
fi
done < $filename
