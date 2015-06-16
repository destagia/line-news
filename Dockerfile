FROM flurdy/oracle-java8
MAINTAINER Shohei Miyashita <kei@kamasu.jp>

# copy project filesroot
ADD ./ /root

# run play
EXPOSE 40005
WORKDIR /root

CMD ./target/universal/stage/bin/line-news -Dhttp.port=80 -J-server
