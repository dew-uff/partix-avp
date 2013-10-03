#!/bin/sh
#set -x

if [ $# -lt 2 ]; then
  echo "Usage: runmpj.sh <conf_file> <jar_file>[OR]<class_file>";
  echo "       For e.g. runmpj.sh ../conf/mpj2.conf ../lib/test.jar";
  echo "       For e.g. runmpj.sh mpj.conf hello.World";
  exit 127
fi

conf=$1
lines=`cat $conf  | egrep -v "#" | egrep "@"`
dir=`pwd`
name=$2
count=0

backslash2slash() {
    echo $1 | sed 's/\\/\//g'
}

CLASSPATH_SEPARATOR=":"
case "`uname`" in
  CYGWIN*) 
    MPJ_HOME=`backslash2slash $MPJ_HOME`
    CLASSPATH=`backslash2slash $CLASSPATH`

    CLASSPATH_SEPARATOR=";"
    ;;
esac


for i in `echo $lines`; do 

  host=`echo $i | cut -d "@" -f 1`
  rank=`echo $i | cut -d "@" -f 3`    

  case "$name" in
    *.jar )


      rsh $host "export MPJ_HOME=~/mpj/; export PATH=$PATH:$MPJ_HOME/bin:$JAVA_HOME/bin; cd ~/mpj; \
          java -cp \"~/mpj/lib/mpj.jar${CLASSPATH_SEPARATOR}$CLASSPATH\" \
               -jar $name $rank $conf niodev $3 $4 $5 $6 $7 $8; rm -f $conf;" &

      ;;

    * )

      rsh $host "export MPJ_HOME=$MPJ_HOME; export PATH=$PATH:$MPJ_HOME/bin:$JAVA_HOME/bin; cd $MPJ_HOME; \
          java -cp \"$MPJ_HOME/lib/mpj.jar${CLASSPATH_SEPARATOR}$CLASSPATH\" \
               $name $count $conf niodev $3 $4 $5 $6 $7 $8; rm -f $conf;" &

      ;;
  esac

  

  count=`expr $count + 1`

done

while [ -f $conf ]
do
sleep 10
done




