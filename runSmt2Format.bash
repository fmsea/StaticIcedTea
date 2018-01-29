#! /bin/bash
domain="dom4"
path="$1"
filename="$2"
type="$3"
while read -r line
do 
   name=( $line )
   echo ${name[0]} "->" ${name[1]}
   class=${name[0]}
   method=${name[1]}
 java -cp .:./bin/ processing.Smt2Format $path $class $method $domain $type
done < $filename
