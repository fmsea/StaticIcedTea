#! /bin/bash
# path to the results, e.g., ./ConditionalTACAS
path="$1"
#file with method and classes
filename="$2"
#analysis type f, c1, c2
type="$3"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
 java -cp .:./bin/ processing.CombineConditionalTimeRD ${path}/resultsRD/ $class $method $type
done < $filename
