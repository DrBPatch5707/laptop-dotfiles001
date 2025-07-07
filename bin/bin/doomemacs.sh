#!/bin/bash

 nohup emacsclient -c -a 'emacs' $@ > /dev/null 2>&1 &

exit
