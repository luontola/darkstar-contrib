A collection of service/manager pairs, miscellaneous utilities and data structures shared by Project Darkstar developers.

# Currently the project contains #

  * [DarkstarIntegrationTest](DarkstarIntegrationTest.md) - An integration testing framework for running tests against a full-featured Darkstar Server
  * [DarkstarRPC](DarkstarRPC.md) - RPC API for simplified communication between Darkstar server and clients
  * [DataStructures](http://code.google.com/p/darkstar-contrib/source/browse/trunk/datastructures/net/gamalocus/sgs/datastructures/) - Currently only a linked list and a reference-reference.
  * [AdminClient](http://code.google.com/p/darkstar-contrib/source/browse/trunk/adminclient/net/gamalocus/sgs/adminclient/) - Provides a means of connecting with Darkstar and doing some "server side" manipulation of the data by means of actions written for specific needs.
  * [DataInspector](http://code.google.com/p/darkstar-contrib/source/browse/trunk/services/net/gamalocus/sgs/services/datainspector/) (Service/Manager) - Provides basic data-browsing functionality to the AdminClient
  * [HotBackup](http://code.google.com/p/darkstar-contrib/source/browse/trunk/services/net/gamalocus/sgs/services/hotbackup) (Service/Manager) - A very simple service that just executes db4.5\_hotbackup at a given interval as long as the server is running (this should be done by an other process in a live environment - but as a snap-shot tool it works quite well)
  * [Identity](http://code.google.com/p/darkstar-contrib/source/browse/trunk/services/net/gamalocus/sgs/services/identity) (Service/Manager) - A very simple service that gives access to the Identity of a transaction


# Other projects for Project Darkstar #

  * [darkstar-exp](http://code.google.com/p/darkstar-exp/) - Experimental changes to the Darkstar core.
  * [darkstar-examples](http://code.google.com/p/darkstar-examples/) - If you are looking for examples to copy-paste from. (would someone start and maintain this project)


# Participate in this project? #

If you have some general purpose code for Project Darkstar that you would like to share - of if you just want to help improve the contributions in this project please leave me a mail on "greisen _at_ gmail _dot_ com"