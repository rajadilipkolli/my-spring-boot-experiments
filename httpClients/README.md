# httpclient

HTTP client is a utility that allows for sending HTTP requests and receiving HTTP responses. It simplifies the process of communicating with other web services and APIs, and makes it easier to integrate your application with external services.
The HTTP client is built on top of the Java URLConnection class and provides a higher level of abstraction for handling HTTP requests and responses. 
It also provides additional features such as automatic handling of authentication and redirects, support for HTTP proxies, and more.
Overall, the HTTP client can help to make your application more efficient and easier to work with when it comes to sending and receiving HTTP requests.

The following table lists all sample codes related to the spring boot integrations with external system.

| Name                                            | Description 		                                                                                                                                       |
|-------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| [http proxy](./boot-http-proxy)                 | The application, demonstrates the way spring boot application will connect with external system using spring 6 http proxy. Logs Request and response using LogBook                            |
| [rest template](./boot-rest-template)           | The application, demonstrates the way spring boot application will connect with external system using rest template                                  |
| [web-client-mvc](./boot-web-client-mvc)         | The application, demonstrates the way spring boot application will connect with external system using spring 5's webclient in a servlet environment  |
| [web-client-webflux](./boot-web-client-webflux) | The application, demonstrates the way spring boot application will connect with external system using spring 5's webclient in a reactive environment |
| [http client](./boot-restclient)                | The application, demonstrates the way spring boot application will connect with external system using spring 6's restclient in a reactive environment |
