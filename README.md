![Cognifide logo](http://cognifide.github.io/images/cognifide-logo.png)

[![Build Status](https://travis-ci.org/Cognifide/knotx.svg?branch=master)](https://travis-ci.org/Cognifide/knotx)

# Knot.x

<p align="center">
  <img src="https://github.com/Cognifide/knotx/blob/master/icons/180x180.png?raw=true" alt="Knot.x Logo"/>
</p>

Knot.x is a lightweight and high-performance **reactive microservice assembler**. It allows you to get rid of all the dynamic data from your content repository and put it into a fast and scalable world of microservices.

We care a lot about speed and that is why we built it on [Vert.x](http://vertx.io/), known as one of the leading frameworks for performant, event-driven applications.

# How it works

## Templating

In order to separate static content and dynamic data we introduced a Templating Engine, which merges a template obtained from the content repository and dynamic data provided by microservices using [Handlebars.js](http://handlebarsjs.com/). Here is what a template looks like:

```html
<script data-api-type="templating" data-call-uri="/path/to/service.json" type="text/x-handlebars-template">
    <h2>{{header}}</h2>
    <div>{{body.content}}</div>
</script>
```

The following table describes all elements and attributes used in the template.

| Element                             | Description                                                              |
| ----------------------------------- | ------------------------------------------------------------------------ |
| `data-api-type="templating"`        | required for **Knot.x** to recognize the script as a template to process |
| `data-call-uri`                     | path to a microsevice that provides the data - it will be handled by a service, as described in the [Configuration](#configuration) section |
| `type="text/x-handlebars-template"` | required by [Handlebars.js](http://handlebarsjs.com/) tool, which is used for templating |
| `{{header}}` `{{body.content}}`| all data in ***double curly braces*** is taken from a JSON response provided by a microservice |

In this case the microservice response could have the following format:

```json
{
    "header" : "Hello",
    "body" : {
        "content": "World"
    }
}
```

## Architecture
The HTTP Request which comes to **Knot.x** causes a request for a template to be sent to one of the available Content Repositories. For each script with `data-api-type="templating"` there is a request to a microservice for the data. After both requests are completed, [Handlebars.js](http://handlebarsjs.com/) merges the static content and the dynamic data and returns a complete document.

![Architecture without load balancer](https://github.com/Cognifide/knotx/blob/master/icons/architecture/without-load-balancer.png)

It's worth mentioning that this architecture scales very easily. Not only can you add as many microservices and repositories as you want, but you can also use multiple Knot.x nodes set up behind a load balancer if you need to handle more traffic.

![Architecture with load balancer](https://github.com/Cognifide/knotx/blob/master/icons/architecture/with-load-balancer.png)

## Flow diagram

The following diagram shows the asynchronous nature of **Knot.x**. After obtaining a template from a repository, we request all the necessary data from microservies, which reduces the time needed for building the whole document.

![Flow diagram](https://github.com/Cognifide/knotx/blob/master/icons/architecture/flow-diagram.png)

# Getting started

## Requirements

To run Knot.x you only need Java 8.

To build it you also need Maven.

## Building

To build it simply checkout the project, run:

```
mvn clean install
```
and find the executable jar in the `target` directory.

Alternatively, you can just download the most recent `.jar` file.

## Executing

To run it execute the following command:

```
java -jar knot.x.jar
```

This will run the server with default settings.

In order to run the server with your own configuration add this to the command:

```
-Dservice.configuration=<path to your service.yml> -Drepository.configuration=<path to your repository.yml>
```

or provide environment variables that will hold locations of your configuration files.

For windows:
```
SET service.configuration=<path to your service.yml>
SET repository.configuration=<path to your repository.yml>
```
For Unix:
```
export service.configuration=<path to your service.yml>
export repository.configuration=<path to your repository.yml>
```

As you may notice, there are two files that need to be defined in order to configure your services and repositories. Please note that the paths should be compatible with the [Spring Resources](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/resources.html) format, for example:

- `file:///data/config.yml` on Linux
- `file:c:\\data\config.yml` on Windows

## Configuration

### Services

Here's how configuration files should look:

**service.yml**
```yaml
services:

  - path: /service/mock/.*
    domain: localhost
    port: 3000

  - path: /service/.*
    domain: localhost
    port: 8080
```

There are two groups of services defined. Each one will be handled by a different server, i.e. all service requests which match the regular expression:

- `/service/mock/.*` will by handled by `localhost:3000`
- `/service/.*` will be handled by `localhost:8080`

The first matched service will handle the request or, if there's no service matched, the corresponding template's script block will be empty. Please note that in the near future it will be improved to define fallbacks in the template for cases when the service does not respond or cannot be matched.

### Repositories

**repository.yml**
```yaml
repositories:

  - type: local
    path: /content/local/.*
    catalogue:

  - type: remote
    path: /content/.*
    domain: localhost
    port: 3001
```

There are two repositories defined - `local` and `remote`. Each of them define `path` - a regular expression that indicates which resources will be taken from this repository. The first one matched will handle the request or, if no repository is matched, **Knot.x** will return a `404 Not found` response for the given request.

Additionally for testing purposes remote repository points mocked template repository endpoint. It works similar to local repository (reads templates from local machine).
**application.yaml**
```yaml
repository:
  mock:
    enabled: true
    port: 3001
    catalogue:
```
The repository can be accessed with url `http://localhost:8092/content/remote/simple.html`.
Mocked template repository endpoint can be simply disabled with `-Drepository.mock.enabled=false`.

#### Local repositories

If you need to take files from a local machine, this is the kind of repository you want to use. It's perfect for mocking data. 

Second parameter to define is `catalogue` - it determines where to take the resources from. If left empty, they will be taken from the classpath. It may be treated like a prefix to the requested resources.

#### Remote repositories

This kind of repository connects with an external server to fetch templates.

To specify where the remote instance is, please configure the `domain` and `port` parameters.

### Using command line arguments and environment variables

Often some properties are sensitive and we do not want to expose them in configuration files, e.g. passwords. In such case we can use command line arguments or environment variables to inject the values of those properties into the configuration.
Let's assume the following repository configuration is present:
```yaml
repositories:

  - type: db
    user: db.user
    password: ${db.password}
```
Since we do not want to expose the database password, we can use a placeholder and substitute it with the value of a command line argument while starting our application:
```
--db.password=passw0rd
```
Another way to provide a value for the password placeholder shown above is to set an evironment variable `db.password`.

>Notice: command line arguments take precedence over environment variables.

# Features

## Requests grouping

Template obtained from the repository may contain many snippets that will trigger microservice calls for data. There is a chance that some of the snippets will have the same `data-call-uri` attribute set, meaning they will request data from the same source.
In such case only one call to microservice shall be made and data retrieved from service call should be applied to all snippets sharing the same `data-call-uri`.

Example:
Let's assume that we obtained the following template from repository:
```html
<div>
<script data-api-type="templating" data-call-uri="/searchService" type="text/x-handlebars-template">
    <div>{{search.term}}</div>
</script>
</div>
...
<div>
<script data-api-type="templating" data-call-uri="/searchService" type="text/x-handlebars-template">
    <ul>
    {{#each search.results}}
      <li>{{result}}<li>
    {{/each}}
    </ul>
</script>
</div>
```
In this case only one call to microservice will be made, since both snippets share the same `data-call-uri`. Data retrived from `/searchService` will be applied to both snippets.

Notice: The following `data-call-uri` attributes
```
/searchService?q=first
```
```
/searchService?q=second
```
would trigger two calls for data because of the difference in query strings, even though the path to service is the same in both.

# Licence

**Knot.x** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)

# Dependencies

- io.vertx
- io.reactivex
- org.springframework 
- org.jsoup
- com.github.jknack.handlebars
- com.google.code.gson
- guava

# Roadmap

- Extend ‘templateDebug’ mode,
- Extend service exception handling (templates should provide information how to behave when a service is not available or returns an error)
- Remote template repository support (for remote repositories like Apache, Redis, AEM Dispatcher)
- Support for POST requests to services (forms)
- Authentication and authorization solution based on JWT Tokens
- Placeholders in service-url for dynamic parameters (e.g. GET/POST/HEADER parameters rewritten to service request). https://github.com/Cognifide/knotx/issues/8
- Multidomain support, single Knot.x cluster can support whole platform with multiple sites. Each domian restricted to set of services to call.
- Docker + Cookbook for easy production setup.
- Kitchen integration tests.
- More Service Repositories implementations, e.g. for Redis.
- More template engines support (e.g. FreeMarker).
