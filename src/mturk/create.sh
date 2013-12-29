#!/bin/sh

q=~/projects/mturk/veriweb.question
p=~/projects/mturk/veriweb.properties
i=~/projects/mturk/veriweb.input 

sh ~/aws-mturk-clt-1.3.0/bin/loadHITs.sh -question $q -properties $p -input $i
