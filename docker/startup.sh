#!/bin/bash
set -e
export service_name=mef-service
export application_pkg=com.gaian

export app_path=/etc/conf/gaian/$service_name

rm -rf $app_path/bootstrap.properties

echo "spring.application.name=$service_name" >> $app_path/bootstrap.properties
echo "spring.cloud.consul.host=$consul_url" >> $app_path/bootstrap.properties
echo "spring.cloud.consul.port=$consul_port" >> $app_path/bootstrap.properties

export jvm_options="$jvm_options -Dspring.profiles.active=ENVIRONMENT -Dlogging.config=$app_path/logback-spring.xml -Dlogfile.location=/var/log/gaian/$service_name/$service_name.log -Dspring.config.location=$app_path/" ;


if [ -z "$apm_url" ];
then
    echo "apm_url is empty"
else
    export jvm_options="$jvm_options -javaagent:/etc/skywalking-apm/skywalking-agent.jar \
        -Dskywalking.collector.backend_service=$apm_url \
        -Dskywalking.agent.service_name=$service_name \
        -Dskywalking.logging.dir=/var/logs/ \
        -Dskywalking.logging.max_file_size=100000
        -Dskywalking.logging.max_history_files=2"
fi

echo "Staring $service_name"

echo "jvm options :  $jvm_options"

exec java $jvm_options -jar mef-service.jar
