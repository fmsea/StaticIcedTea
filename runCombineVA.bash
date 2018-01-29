#! /bin/bash
#the path to the files
path="$1"
#file name containing (class, method) entries
filename="$2"
#c1 for pseudo-conditional and c2 for conditional
type="$3"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
 java -cp .:./bin/ processing.CombinePartialTimeScript $path/resultsVA $class $method dom4 $type
done < $filename
