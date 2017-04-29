#! /bin/bash
export DYLD_LIBRARY_PATH=~/Documents/z3Java/z3/build/:.
echo $DYLD_LIBRARY_PATH
domain="dom9"
filename="$1"
writeToFile="y"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
 java -cp .:./bin/ processing.CombinePartialTimeScript $class $method $domain
done < $filename
