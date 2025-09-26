FROM liquibase/liquibase:4.31.1

# Копирование конфигурации и миграций
COPY ./* /liquibase/

# Установка правильного JDBC-драйвера
ENV CASSANDRA_JDBC_VERSION=4.14.0
ENV CASSANDRA_EXT_VERSION=4.31.1
RUN mkdir -p /liquibase/lib && cd /liquibase/lib \
    && wget \
    https://repo1.maven.org/maven2/org/liquibase/ext/liquibase-cassandra/${CASSANDRA_EXT_VERSION}/liquibase-cassandra-${CASSANDRA_EXT_VERSION}.jar \
    && wget \
    https://github.com/ing-bank/cassandra-jdbc-wrapper/releases/download/v4.14.0/cassandra-jdbc-wrapper-4.14.0-bundle.jar

CMD ["liquibase", "--changelog-file=changelog.xml", "update"]
#CMD ["/usr/bin/bash", "-c", "sleep 3600"]