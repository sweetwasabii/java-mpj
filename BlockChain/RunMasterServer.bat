@echo off

set workspace=C:\Users\Admin\IdeaProjects\BlockChain\out\artifacts\MasterServer_jar
echo %workspace%
cd %workspace%
start rmiregistry & java -Djava.security.policy=policy -jar MasterServer.jar