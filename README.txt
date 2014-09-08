PASSO A PASSO DA EXECUÇÃO LOCAL
0 - Editar PartixVPConfig.xml com os dados dos nodes
host:porta

1 - Rodar NodeQueryProcessorEngine.java
localhost 3001 dblp SYSTEM MANAGER 0 1

2 - Rodar CQP_Scheduler_Engine.java
8050 "input/CQP.conf"

3 - Rodar ClusterQueryProcessorAdmin.java
localhost 8050 SET_CLUSTER_SIZE 2

4 - Rodar ConnectionManagerImpl.java
8051 "input/PartiXVPConfig.xml"

5 - Rodar TestAvpLoadBalancing
localhost 8050 2 1 1 NO_SYSMON