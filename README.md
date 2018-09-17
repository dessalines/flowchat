[FlowChat](http://flow-chat.com) &mdash; An open-source, self-hostable reddit alternative featuring communities and live-updating threaded conversations.
==========
![](http://img.shields.io/version/0.3.1.png?color=green)
[![Build Status](https://travis-ci.org/dessalines/flowchat.svg?branch=master)](https://travis-ci.org/dessalines/flowchat)

<!---
	FlowChat: An open-source, self-hostable reddit alternative featuring communities and live-updating threaded conversations.

	Hey /r/blank, leftist programmer here. I've made a reddit alternative called Flowchat, featuring communities, and live updating threaded conversations. Self-hostable, open-source, explicitly anti-racist.

	Description from the [github](https://github.com/dessalines/flowchat):

> [FlowChat](http://flow-chat.com) is an open-source, self-hostable reddit alternative. It has communities, hashtags, **live-updating** threaded conversations, and voting.

> Flowchat tries to solve the problem of having a fluid, free-feeling group chat, while allowing for side conversations so that every comment isn't at the top level, and doesn't disrupt the flow. 

> Multiple conversations can take place **at once**, without interrupting the flow of the chatroom.

> Check out the default community, [vanilla](http://flow-chat.com/#/community/1), or create your own.

> It features:
- Private or public discussions and communities.
- Sorting by recentness, hotness, or popularity.
- Antiracist policies including a global slur filter (No racism will be allowed on the main Flowchat instance).
- Image and video focused, with auto-zoom.
- Moderation including blocking users, appointing moderators, or deleting comments.
- NSFW filtering.
- Stickied posts.
- Hashtags.

	I'd also like to invite the moderators of this sub to take over the equivalent leftist community on it. Message my reddit user if your community's already been created and I'll gladly do this. 
-->

[FlowChat](http://flow-chat.com) is an open-source, self-hostable reddit alternative. It has communities, hashtags, **live-updating** threaded conversations, and voting.

Flowchat tries to solve the problem of having a fluid, free-feeling group chat, while allowing for side conversations so that every comment isn't at the top level, and doesn't disrupt the flow. 

Multiple conversations can take place **at once**, without interrupting the flow of the chatroom.

Check out the default community, [vanilla](http://flow-chat.com/#/community/1), or create your own.

It features:
- Private or public discussions and communities.
- Sorting by recentness, hotness, or popularity.
- Antiracist policies including a global slur filter (No racism will be allowed on the main Flowchat instance).
- Image and video focused, with auto-zoom.
- Moderation including blocking users, appointing moderators, or deleting comments.
- NSFW filtering.
- Stickied posts.
- Hashtags.

Tech used:
- [Java Spark](https://github.com/perwendel/spark), [Bootstrap v4](https://github.com/twbs/bootstrap), [Angular.io](https://github.com/angular/angular), [Angular-cli](https://github.com/angular/angular-cli), [ngx-bootstrap](http://valor-software.com/ngx-bootstrap/), [ActiveJDBC](http://javalite.io/activejdbc), [Liquibase](http://www.liquibase.org/), [Postgres](https://www.postgresql.org/), [Markdown-it](https://github.com/markdown-it/markdown-it), [angular2-toaster](https://github.com/Stabzs/Angular2-Toaster)

Check out a sample discussion [here](http://flow-chat.com/#/discussion/13).

Join the community: [flowchat](https://www.reddit.com/r/flowchat/)

[Change log](https://github.com/dessalines/flowchat/issues?q=is%3Aissue+is%3Aclosed)

![screen1](https://i.imgur.com/hDeDamH.png)

![screen2](https://i.imgur.com/yTKwfhd.png)



---

## Installation 

*If you want to self-host or develop flowchat.*

### Docker

#### Requirements

- Docker
- docker-compose

#### Start the docker instance

```sh
git clone https://github.com/dessalines/flowchat
cd flowchat
// edit ARG ENDPOINT_NAME=http://localhost:4567 in ./Dockerfile to your hostname
docker-compose up
```

Goto to http://localhost:4567

### Local development

#### Requirements
- Java 8 + Maven
- Node + npm/yarn, [nvm](https://github.com/creationix/nvm) is the preferred installation method.
- angular-cli: `npm i -g @angular/cli`
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
sorting_created_weight=86400
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

---

## Bugs and feature requests

Have a bug or a feature request? If your issue isn't [already listed](https://github.com/dessalines/flowchat/issues/), then open a [new issue here](https://github.com/dessalines/flowchat/issues/new).
