#! /bin/bash
filename="$1"
type="$2"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
 java -cp .:./bin/ processing.CombineConditionalTimeRD $class $method $type
done < $filename
