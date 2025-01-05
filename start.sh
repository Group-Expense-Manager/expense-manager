#!/bin/bash

REPO="expense-manager"

DIR=`realpath -L ./$REPO`

echo going to compute stats for $DIR

cd $DIR

git checkout main
git pull
git log --since='last year' --date=short --pretty="%x40%h%x2C%an%x2C%ad%x2C%x22%s%x22%x2C" --shortstat | tr "\n" " " | tr "@" "\n" > res.csv

sed -i 's/ files changed//g' res.csv
sed -i 's/ file changed//g' res.csv
sed -i 's/ insertions(+)//g' res.csv
sed -i 's/ insertion(+)//g' res.csv
sed -i 's/ deletions(-)//g' res.csv
sed -i 's/ deletion(-)//g' res.csv

mv res.csv git-log.csv

awk -i inplace -v repo="$REPO" 'BEGINFILE{print "repo,commit,user,date,comment,files changed,insertions,deletions"}{print repo","$0}' git-log.csv

sed -i '/^$/d' git-log.csv

cd -
