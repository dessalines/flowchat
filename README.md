[FlowChat](https://flowchat.tk) &mdash; A live updating, threaded discussion platform with voting. 
==========
![](http://img.shields.io/version/0.1.0.png?color=green)
[![Build Status](https://travis-ci.org/tchoulihan/flowchat.png)](https://travis-ci.org/tchoulihan/flowchat)

[FlowChat](https://flowchat.tk) is an open-source, self-hostable, live-updating discussion platform, featuring chatrooms with threaded conversations, and voting. 

Flowchat tries to solve the problem of having a fluid, free-feeling group chat, while allowing for side conversations so that every comment doesn't interrupt the flow, and so multiple conversations can take place at once. 

It features:
- A complete chat application with live updating, and threaded discussion.
- Private or public discussions
- Customizable discussion and comment sorting by recentness, and popularity.
- Discussion hashtags
- Discussion creators can block users, or delete comments
- [Range voting](http://rangevoting.org/UniqBest.html), not simple thumbs up or thumbs down, for mass expressiveness, for both comments and discussions.

Tech used:
- [Java Spark](https://github.com/perwendel/spark), [Bootstrap v4](https://github.com/twbs/bootstrap), [Angular2](https://github.com/angular/angular), [Angular-cli](https://github.com/angular/angular-cli), [ng2-bootstrap](http://valor-software.com/ng2-bootstrap/), [ActiveJDBC](http://javalite.io/activejdbc), [Liquibase](http://www.liquibase.org/), [Postgres](https://www.postgresql.org/), [Markdown-it](https://github.com/markdown-it/markdown-it), [angular2-toaster](https://github.com/Stabzs/Angular2-Toaster)

Check out a sample discussion [here](https://flowchat.tk/discussion/1).

Join the subreddit: [/r/flowchat](https://www.reddit.com/r/flowchat/)

[Change log](https://github.com/tchoulihan/flowchat/releases)

## Screenshots:
<img src="http://i.imgur.com/lZBMsn5.png">
<img src="http://i.imgur.com/hwNc0mx.png">
<img src="http://i.imgur.com/JbOBf1h.png">
<img src="http://i.imgur.com/chhvZwC.png">

## Installation 

*If you want to self-host flowchat.*

### Requirements
- Java 8
- Nodejs/npm, [nvm](https://github.com/creationix/nvm) is the preferred installation method.
- Postgres 9.3 or higher

### Download Flowchat
`git clone https://github.com/tchoulihan/flowchat`

### Setup a postgres database
[Here](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-16-04) are some instructions to get your DB up and running.

### Edit your pom.xml file to point to your database
```sh
cd flowchat
vim service/pom.xml
```

Edit it to point to your own database
```xml
<!--The Database location and login, here's a sample-->
<jdbc.url>jdbc:postgresql://127.0.0.1/flowchat</jdbc.url>
<jdbc.username>postgres</jdbc.username>
<jdbc.password></jdbc.password
<!--The sorting for discussions, comments, and tags are:
 	Sorting score =
		created_weight/(now_seconds - comment_seconds) +
		number_of_votes*number_of_votes_weight +
		avg_rank*avg_rank_weight-->
<sorting_created_weight>100000</sorting_created_weight>
<sorting_number_of_votes_weight>0.001</sorting_number_of_votes_weight>
<sorting_avg_rank_weight>0.01</sorting_avg_rank_weight>
```
### Install flowchat
`./install.sh`

Load up the homepage at http://localhost:4567

You can redirect ports in linux to route from port 80 to this port:

`sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 4567`

## Bugs and feature requests
Have a bug or a feature request? If your issue isn't [already listed](https://github.com/tchoulihan/flowchat/issues/), then open a [new issue here](https://github.com/tchoulihan/flowchat/issues/new).
