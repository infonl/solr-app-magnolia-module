Solr App Magnolia Module
=======================

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) containing an [app](https://documentation.magnolia-cms.com/display/DOCS/Apps) for [Magnolia CMS](http://www.magnolia-cms.com) 
that acts as an optional add-on to the Magnolia Solr Search Provider and Magnolia Solr Content Indexer modules.

It offers functionality to inspect basic aspects of the Solr search server configured in the Solr Search Provider module and allows users to perform basic administration tasks 
such as clearing the Solr index and starting any Solr crawlers that have been enabled in the Magnolia Content Indexer module.

![Solr App Screenshot](https://raw.githubusercontent.com/infonl/solr-app-magnolia-module/master/img/solr-app-screenshot.png?raw=true)

License
-------
Released under the MIT License, see LICENSE.txt. 

Feel free to use this app, but if you modify the source code please fork this project on Github.

Magnolia Forge
--------------
Though the source code is kept on Github, this module is part of the [Magnolia Forge](http://forge.magnolia-cms.com/) and uses its infrastructure.

Issue tracking
--------------
Please report any bugs, improvements or feature requests at the [Magnolia JIRA project for this Magnolia Forge module](https://jira.magnolia-cms.com/projects/SOLRAPP).

Maven artifacts in Magnolia's Nexus
---------------------------------
The code is built on [Magnolia's Jenkins](https://jenkins.magnolia-cms.com/view/Forge/job/forge_solr-app-magnolia-module/), and Maven artifacts are available through [Magnolia's Forge release repository](http://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/) and [Forge release repository](https://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/). 

You can browse available artifacts through [Magnolia's Nexus](http://nexus.magnolia-cms.com/)

Maven dependency
-----------------
```xml
        <dependency>
            <groupId>nl.info.magnolia</groupId>                  
            <artifactId>solr-app-magnolia-module</artifactId>
            <version>1.0.1</version>
        </dependency>
```

Versions
-----------------
Version 1.0.x is compatible with Magnolia 5.3.x and 5.4.x

Magnolia Module Configuration
-----------------
The module itself cannot be configured. It uses the configuration of the Magnolia Solr Search Provider and Content Indexer modules 