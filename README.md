# Motivtion
A common demand on search applications for libraries is, to support synonymes from authority records. *(The request "author:Blair, Eric" should find also the better known pseudonym "George Orwell")*

In traditional a OPAC based on a SQL database, this may solved generically with a join.
Modern bibliographic search systems are mostly based on full text retrieval systems like Lucene/SOLR/ElasticSearch/...
Meanwhile some of this back ends may emulate a 'join', they are still key-value stores. For this it will be in 
most cases better, to expand the authority records external. 

### Expanding authority records while indexing vs. while searching
* Expanding the synonymes while searching is a straight forward strategy, but it is hard to handle complex synonymes like "big apple" to "new york city". Also it may limit the response time of the system.
* Expanding the synonymes while searching is not that flexible, but at index time the kind of the authority record (topic term, personal name, ...) is known. So it is easy to handle complex synonyms.
 
### Static file vs. service for synonymes
* A static file is easy to handle, but for a great collection of authority records may grow to a size of some Gb. This doesn't matte for a complete build of the index, but loading such a big file for every update of a bibliographic record is inefficient.
* A background service is slight more complex, but does not slow down the startup of the index or the update. On the other hand, a service may increase the time needed to build a new index. This disadvantage can be avoided with a cache. 

# Description
This project contains a complete service to expand the 'GND'. (Authority records provided by the [German National Library](http://www.dnb.de/EN/Home/home_node.html)) 

The service has three Parts
* [Code](GndAuthorityRecords/src/de/hebis/it/hds/gnd/in) to parse the authority records (provided in MarcXML) and load them into a simple Solr index.
* A minimal configuration for the Solr index
* [Exemplary code](GndAuthorityRecords/src/de/hebis/it/hds/gnd/in) to integrate the preprocessed synonymes into the own indexing process. e.g. [SolrMarc](https://github.com/solrmarc) 

# Status
The main skeleton is quite stable but the processing of the data is in progress
###  Initial data
The offline package of the GND is seperated in disjunkt files
* T_umlenk_loesch1701.mrc.xml - (todo)
* Tbgesamt1701gnd.mrc.xml - (todo)
* Tfgesamt1701gnd.mrc.xml - (todo)
* Tggesamt1701gnd.mrc.xml - (todo)
* Tngesamt1701gnd.mrc.xml - (todo)
* Tpgesamt1701gnd.mrc.xml - Personal Names (in progress)
* Tsgesamt1701gnd.mrc.xml - Topic Terms (done)
* Tugesamt1701gnd.mrc.xml - (todo)
#### Online update
Changes in the GND are available via OAI
* OaiUpdates - All kind (todo)

# Notes
1. The code and the config for Solr contains some optional features, beside the synonyms
2. The approach can easy extended for authority records from additional/other sources
3. The source contains a URL to a local installation of Solr. This resource is not public available.  

### JavaDoc
You can find the precompiled javadoc below [doc](GndAuthorityRecords/doc) 

### Compatibility
The code uses features of Java8 and needs libraries from following projects:
* [Log4j 2](https://logging.apache.org/log4j/2.x/)
* [SolrJ](http://lucene.apache.org/solr/)
* [HdsToolkit](HdsToolkit)
