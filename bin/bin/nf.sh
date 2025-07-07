!#/bin/bash
start_dir=$(pwd);
cd;
file=$(fzf --preview="bat --color=always {}")
if [$1 == "-s" ] || [ $1 == "-S"]; then
sudo nano $file
else
nano $file
fi
cd $start_dir;
exit;
