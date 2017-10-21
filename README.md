[FlowChat](http://flow-chat.com) &mdash; An open-source, live updating, threaded chat platform with range voting. 
==========
![](http://img.shields.io/version/0.3.1.png?color=green)
[![Build Status](https://travis-ci.org/dessalines/flowchat.svg?branch=master)](https://travis-ci.org/dessalines/flowchat)

<!---
	FlowChat: a live-updating, threaded discussion app, featuring reddit-like communities, and slack-like chatrooms. Self-hostable, open-source, written in java and angular2.

	Hey /r/blank, marxist and programmer here. I made an app called FlowChat: a live-updating, threaded discussion app, featuring reddit-like communities, and slack-like chatrooms. Self-hostable, open-source.
-->

[FlowChat](http://flow-chat.com) is an open-source, self-hostable, **live-updating** discussion platform, featuring communities, discussions with threaded conversations, and voting.

It can act as an alternative to forums, as a private team communication platform(like slack), a content creation platform(like reddit), or a voting/polling platform like [referendum](https://referendum.ml).

Flowchat tries to solve the problem of having a fluid, free-feeling group chat, while allowing for side conversations so that every comment isn't at the top level. Multiple conversations can take place **at once**, without interrupting the flow of the chatroom.

It uses [range voting](http://rangevoting.org/UniqBest.html)(also known as olympic score voting) for sorting comments, discussions, and communities. Range voting is *more expressive* than simple :thumbsup: or :thumbsdown: votes.

Check out the default community, [vanilla](http://flow-chat.com/#/community/1), or create your own.

It features:
- A complete discussion platform with communities, and live-updating threaded discussions.
- Private or public discussions and communities
- Customizable sorting, by recentness, and popularity.
- Hashtags for discussions and communities. 
- Discussion/Community creators can block users, appoint moderators, or delete comments.

Tech used:
- [Java Spark](https://github.com/perwendel/spark), [Bootstrap v4](https://github.com/twbs/bootstrap), [Angular2](https://github.com/angular/angular), [Angular-cli](https://github.com/angular/angular-cli), [ng2-bootstrap](http://valor-software.com/ng2-bootstrap/), [ActiveJDBC](http://javalite.io/activejdbc), [Liquibase](http://www.liquibase.org/), [Postgres](https://www.postgresql.org/), [Markdown-it](https://github.com/markdown-it/markdown-it), [angular2-toaster](https://github.com/Stabzs/Angular2-Toaster)

Check out a sample discussion [here](http://flow-chat.com/#/discussion/13).

Join the subreddit: [/r/flowchat](https://www.reddit.com/r/flowchat/)

[Change log](https://github.com/dessalines/flowchat/issues?q=is%3Aissue+is%3Aclosed)

==========

## Installation 

*If you want to self-host or develop flowchat.*

### Local development

#### Requirements
- Java 8 + Maven
- Node + npm/yarn, [nvm](https://github.com/creationix/nvm) is the preferred installation method.
- angular-cli: `npm i -g angular-cli@latest`
- Postgres 9.3 or higher

#### Download Flowchat
`git clone https://github.com/dessalines/flowchat`

#### Setup a postgres database

[Here](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-16-04) are some instructions to get your postgres DB up and running.

```sh
psql -c "create user flowchat with password 'asdf' superuser"
psql -c 'create database flowchat with owner flowchat;'
```

#### Edit your pom.xml file to point to your database
```sh
cd flowchat
vim service/flowchat.properties
```

Edit it to point to your own database:
```
<!--The Database location and login, here's a sample-->
jdbc.url=jdbc\:postgresql\://127.0.0.1/flowchat
jdbc.username=flowchat
jdbc.password=asdf
sorting_created_weight=3600
sorting_number_of_votes_weight=0.001
sorting_avg_rank_weight=0.01
reddit_client_id=
reddit_client_secret=
reddit_username=
reddit_password=
```
#### Install flowchat

for local testing: 

`./install_dev.sh` and goto http://localhost:4567/

for front end angular development, do:

```
cd ui
ng serve
```

and goto http://localhost:4200

for a production environment, edit `ui/config/environment.prod.ts` to point to your hostname, then run:

`./install_prod.sh`

You can redirect ports in linux to route from port 80 to this port:

`sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 4567`

==========

## Bugs and feature requests
Have a bug or a feature request? If your issue isn't [already listed](https://github.com/dessalines/flowchat/issues/), then open a [new issue here](https://github.com/dessalines/flowchat/issues/new).
