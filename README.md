<p id="start" align="center">
<br>
<a href="#start"><img height="130rem" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/logo.png"></a>
<br><br>
<a href="#start"><img width="250rem" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/logo_text.png"></a>
<h1></h1>
</p>

<a title="License MIT" href="https://github.com/xnbox/DeepfakeHTTP/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue?style=flat-square"></a>
<a title="Release 6.2.1" href="https://github.com/xnbox/DeepfakeHTTP/releases"><img src="https://img.shields.io/badge/release-6.2.1-4DC71F?style=flat-square"></a>
<a title="Powered by Tommy" href="https://github.com/xnbox/tommy"><img src="https://img.shields.io/badge/powered_by-Tommy-blueviolet?style=flat-square"></a>
<br>
<p id="banner" align="center">
<br>
<a href="#banner"><img width="98%" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/text.png" alt="YOUR 100% STATIC DYNAMIC BACKEND"></a>
<table>
<tr>
<td>
<a href="#banner"><img align="left" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/image1.png" width="190"></a>
<h3>What are people using it for?</h3>
<ul>
    <li>Creating the product PoC or demo before even starting out with the backend</li>
    <li>REST, GraphQL, and other APIs mocking and testing</li>
    <li>Hiding critical enterprise infrastructure behind a simple static facade</li>
    <li>Hacking and fine-tuning HTTP communications on both server and client sides</li>
</ul>
<img width="1000" height="0">
</td>
</tr>
</table>
</p>
<p align="center">
<a href="#get-started">Get started</a> | <a href="#usage">Usage</a> | <a href="#usage-exampes">Usage Examples</a> | <a href="#how-does-it-work">How does it work?</a> | <a href="#features">Features</a> | <a href="#appendix-boptional-request--response-headers">Optional Headers</a> | <a href="#appendix-acommand-line-options">CLI</a> | <a href="Cheatsheet.md">Cheatsheet</a> | <a href="#">FAQ</a>
</p>

<h2>Get started</h2>

<ol>
    <li>Download the <a href="https://github.com/xnbox/DeepfakeHTTP/releases/latest">latest release</a> of <code>df.jar</code></li>
    <li>Copy-paste the content of the dump example to the file <code>dump.txt</code>:
<span></span>

```http
GET /api/customer/123 HTTP/1.1

HTTP/1.1 200 OK
Content-Type: application/json

{
    "id": 123,
    "fname": "John",
    "lname": "Doe",
    "email": ["john@example.com", "johndoe@example.com"]
}
```

</li>
    <li>Start the server from command line:
<pre>
java -jar df.jar --dump dump.txt
</pre>
</li>
    <li>Use a browser to check whether the server is running:
<br>
<pre><a href="http://localhost:8080/api/customer/123">http://localhost:8080/api/customer/123</a></pre>
</li>
<li>Get response:<br>
<a href="#get-started"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/get-started.png"></a>
</li>
</ol>
That's it!
<br>
For more examples see the <a href="Cheatsheet.md">cheatsheet</a>.

<h2>Usage</h2>

```
java -jar df.jar [OPTIONS] [FLAGS] [COMMANDS]                                 
                                                                              
OPTIONS:                                             
   --host <host name>      host name, default: localhost
   --port <number>         HTTP TCP port number, default: 8080                
   --port-ssl <number>     HTTPS TCP port number, default: 8443               
   --dump <file|url>...    dump text file(s) and/or OpenAPI json/yaml file(s) 
   --db <file|url>         json/yaml/csv memory file to populate templates    
   --db-export <file>      export memory to json file                         
   --db-path <path>        serve live memory file at specified context        
   --js <file|url>...      JavaScript file(s) for script engine context       
   --openapi-path <path>   serve built-in OpenAPI client at specified context 
   --openapi-title <text>  provide custom OpenAPI specification title         
   --collect <file>        collect live request/response to file              
   --format <json|yaml>    output format for --print-* commands, default: json
   --status <number>       status code for non-matching requests, default: 404
   --max-log-body <number> max body bytes in console log, default: unlimited  
                                                                              
FLAGS:                                                                        
   --no-log                disable request/response console logging           
   --no-log-request-info   disable request info in console logging
   --no-log-headers        disable request/response headers in console logging
   --no-log-body           disable request/response body in console logging   
   --no-cors               disable CORS headers                               
   --no-etag               disable 'ETag' header                              
   --no-server             disable 'Server' header                            
   --no-watch              disable watch files for changes                    
   --no-color              disable ANSI color output for --print-* commands   
   --no-pretty             disable prettyprint for --print-* commands         
   --no-template           disable template processing                        
   --no-wildcard           disable wildcard processing                        
   --no-bak                disable backup old memory file before overwrite    
   --strict-json           enable strict JSON comparison                      
   --redirect              enable redirect HTTP to HTTPS                      
                                                                              
COMMANDS:                                                                     
   --help                  print help message                                 
   --print-info            print dump files statistics to stdout as json/yaml 
   --print-requests        print dump requests to stdout as json/yaml         
   --print-openapi         print OpenAPI specification to stdout as json/yaml 
```

<h2>Usage Exampes</h2>
Start server on dump file:
<pre>
java -jar df.jar --dump dump.txt
</pre>
Start server on OpenAPI file:
<pre>
java -jar df.jar --dump openapi.json
</pre>
Start server with built-in OpenAPI client:
<pre>
java -jar df.jar --openapi-path /api --dump dump.txt
</pre>
<details>
<summary>
    more examples&hellip;
</summary>
<br>
Start server on few dump files:
<pre>
java -jar df.jar --dump dump1.txt dump2.txt dump3.txt
</pre>
Start server on mix of dump and OpenAPI files:
<pre>
java -jar df.jar --dump dump1.txt openapi2.json dump3.txt openapi4.yaml
</pre>
Start server with built-in OpenAPI client with custom title:
<pre>
java -jar df.jar --openapi-path /api --openapi-title 'My Killer REST API v18.2.1' --dump dump.txt
</pre>
</details>
<details>
<summary>
	even more examples&hellip;
</summary>
<br>
Collect live request/response to file:
<pre>
java -jar df.jar --collect /home/john/live.txt --dump dump.txt
</pre>
Specify JSON data file to populate templates:
<pre>
java -jar df.jar --data /home/john/data.json --dump dump.txt
</pre>
Print dump files statistics to stdout as JSON:
<pre>
java -jar df.jar --print-info --dump dump.txt
</pre>
Print dump requests to stdout as JSON:
<pre>
java -jar df.jar --print-requests --dump dump.txt
</pre>
Print OpenAPI specification to stdout as JSON:
<pre>
java -jar df.jar --print-openapi --dump dump.txt
</pre>
</details>
If you still need examples make sure to check out the <a href="Cheatsheet.md">cheatsheet</a>.

<h2>Prerequisites</h2>
<ul>
    <li>Java 15 or above</li>
</ul>

<h2>How does it work?</h2>
<ol>
	<li>Got the client's request.</li>
	<li>Search the dump for corresponded entry (request-response pair) by matching all specified request's parts:<br>
	<i>method</i>, <i>URI</i>, <i>headers</i>, and <i>body</i>.</li>
	<li>If the entry was found, the server sends the appropriate response to the client.</li>
	<li>If the entry was not found, the server sends a status <code>404</code>.</li>
</ol>
That's all.

<h2>Features</h2>

&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;no dependencies<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;no installation<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;no configs<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;crossplatform<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;single-file executable<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;command-line interface<br>
<details>
<summary>
    more features&hellip;
</summary>
<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;fully asynchronous<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;HTTP message formats RFC 7230<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;multiple entries per dump<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;multiple request/response entries per dump<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;extracts responses from HTTP dumps and OpenAPI JSON/YAML<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;self-hosted built-in OpenAPI client<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;exportable persistent memory<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;persistent data<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;CGI, XGI and JavaScript handlers<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;<code>GET</code>, <code>HEAD</code>, <code>POST</code>, <code>PUT</code>, <code>DELETE</code> etc.<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;multi-line and multi-value headers RFC 7230<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;openAPI-styled templates in paths<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;wildcards ( <code> *</code> and <code> ?</code> with escape <code> /</code> ) in query string and header values<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;templates in URI, headers, body<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;body fetching from external sources like URLs, local files, and data URI<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;per entry user-defined request/response delays<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;comments <code> #</code> in dumps<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;live request/response collection<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;watching dump files for changes<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;<code>ETag</code> support<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;CORS support<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;live request/response logging<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;TLS(SSL) connections and HTTP to HTTPS redirect<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;customizable OpenAPI client path<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;latest OpenAPI specification <code>v3.0.3</code> support<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;colorized console output<br>
&nbsp;&nbsp;&nbsp;&nbsp;&check;&nbsp;&nbsp;disabling color via command line or <code>NO_COLOR</code> environment variable<br>
</details>

<h2>LICENSE</h2>
The DeepfakeHTTP is released under the <a href="https://github.com/xnbox/DeepfakeHTTP/blob/main/LICENSE">MIT</a> license.
<br><br>
<h1></h1>
<br><br>


<h2>
APPENDIX A.
<br>
Command line options
</h2>
<table>
    <tr><th width="226rem">Option</th><th>Default</th><th>Description</th></tr>

<tr></tr>
    <tr id="cli-host"><td valign="top"><code>--host &lt;host name&gt;</code>
    </td>
    <td valign="top" align="right"><code>localhost</code></td>
    <td valign="top">
    host name, default: <code>localhost</code><br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-port-ssl"><code>--port &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-port-ssl"><code>--port-ssl &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-redirect"><code>--redirect</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-port"><td valign="top"><code>--port &lt;number&gt;</code>
    </td>
    <td valign="top" align="right"><code>8080</code></td>
    <td valign="top">
    HTTP TCP port number, default: <code>8080</code><br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-port-ssl"><code>--port-ssl &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-redirect"><code>--redirect</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-host"><code>--host &lt;host name&gt;</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-port-ssl"><td valign="top"><code>--port-ssl &lt;number&gt;</code>
    </td>
    <td valign="top" align="right"><code>8443</code></td>
    <td valign="top">
    HTTPS TCP port number, default: <code>8443</code><br>
    Create TLS(SSL) connection based on built-in self-signed certificate<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-port"><code>--port &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-redirect"><code>--redirect</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-host"><code>--host &lt;host name&gt;</code></a>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--dump &lt;file|url&gt;...</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Dump text file(s) and/or OpenAPI json/yaml file(s)
</td></tr>

<tr></tr>
    <tr id="cli-db"><td valign="top"><code>--db &lt;file|url&gt;...</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    JSON/YAML/CSV memory file to populate templates<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db-export"><code>--db-export &lt;file&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db-path"><code>--db-path &lt;path&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-bak"><code>--no-bak</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-js"><code>--js &lt;file|url&gt;...</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-JS"><code>X-Handler-JS</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-db-export"><td valign="top"><code>--db-export &lt;file&gt;</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Export memory to JSON file<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db"><code>--db &lt;file|url&gt;...</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db-path"><code>--db-path &lt;path&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-bak"><code>--no-bak</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-db-path"><td valign="top"><code>--db-path &lt;path&gt;</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Serve live memory file at specified context.<br>
    With this option you can view or export the memory state in JSON format.<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db"><code>--db &lt;file|url&gt;...</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db-export"><code>--db-export &lt;file&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-bak"><code>--no-bak</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-js"><td valign="top"><code>--js &lt;file|url&gt;...</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    JavaScript file(s) for script engine context.<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-JS"><code>X-Handler-JS</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db"><code>--db &lt;file|url&gt;...</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-openapi-path"><td valign="top"><code>--openapi-path &lt;path&gt;</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Serve built-in OpenAPI client at specified context<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-openapi-title"><code>--openapi-title &lt;text&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-print-openapi"><code>--print-openapi</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-openapi-title"><td valign="top"><code>--openapi-title &lt;text&gt;</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Provide custom OpenAPI specification title<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-openapi-path"><code>--openapi-path &lt;path&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-print-openapi"><code>--print-openapi</code></a>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--collect &lt;file&gt;</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Collect live request/response to file
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--format &lt;json|yaml&gt;</code>
    </td>
    <td valign="top" align="right"><code>json</code></td>
    <td valign="top">
    Output format for <code>--print-*</code> commands, default: <code>json</code>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--status &lt;number&gt;</code>
    </td>
    <td valign="top" align="right"><code>404</code></td>
    <td valign="top">
    Status code for non-matching requests, default: <code>404</code>
</td></tr>

<tr></tr>
    <tr id="cli-max-log-body"><td valign="top"><code>--max-log-body &lt;number&gt;</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Max body bytes in console log, default: unlimited<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log"><code>--no-log</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-body"><code>--no-log-body</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-headers"><code>--no-log-headers</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-no-log"><td valign="top"><code>--no-log</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable request/response console logging<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-request-info"><code>--no-log-request-info</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-body"><code>--no-log-body</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-max-log-body"><code>--max-log-body &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-headers"><code>--no-log-headers</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-no-log-request-info"><td valign="top"><code>--no-log-request-info</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable request info in console logging<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-body"><code>--no-log-body</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-max-log-body"><code>--max-log-body &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-headers"><code>--no-log-headers</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-no-log-headers"><td valign="top"><code>--no-log-headers</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable request/response headers in console logging<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log"><code>--no-log</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-request-info"><code>--no-log-request-info</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-body"><code>--no-log-body</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-max-log-body"><code>--max-log-body &lt;number&gt;</code></a>
</td></tr>

<tr></tr>
    <tr id="cli-no-log-body"><td valign="top"><code>--no-log-body</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable request/response body in console logging<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-max-log-body"><code>--max-log-body &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log"><code>--no-log</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-request-info"><code>--no-log-request-info</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-log-headers"><code>--no-log-headers</code></a>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--no-cors</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable CORS headers
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--no-etag</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable <code>ETag</code> header
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--no-server</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable <code>Server</code> header
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--no-watch</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable watch files for changes
</td></tr>

<tr></tr>
    <tr id="cli-no-color"><td valign="top"><code>--no-color</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable ANSI color output for <code>--print-*</code> commands<br>
    ANSI color output also can be disabled via <code>NO_COLOR</code> environment variable.<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-pretty"><code>--no-pretty</a></code>
</td></tr>

<tr></tr>
    <tr id="cli-no-pretty"><td valign="top"><code>--no-pretty</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable prettyprint for <code>--print-*</code> commands<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-color"><code>--no-color</code></a>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--no-template</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable template processing
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--no-wildcard</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable wildcard processing.<br>
    By default wildcard processing is enabled.<br>
    The asterisk <code>*</code> represents one or more characters, the question mark <code>?</code> represents a single character, and <code>/</code> represents escape character.
</td></tr>

<tr></tr>
    <tr id="cli-no-bak"><td valign="top"><code>--no-bak</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Disable backup old memory file before overwrite.<br>
    The memory file is overwritten every time the server shuts down.<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db"><code>--db &lt;file|url&gt;...</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db-export"><code>--db-export &lt;file&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db-path"><code>--db-path &lt;path&gt;</code></a>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--strict-json</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Enable strict (byte by byte) JSON comparison
</td></tr>

<tr></tr>
    <tr id="cli-redirect"><td valign="top"><code>--redirect</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Enable redirect HTTP to HTTPS<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-port"><code>--port &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-port-ssl"><code>--port-ssl &lt;number&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-host"><code>--host &lt;host name&gt;</code></a>
</td></tr>

<tr></tr>
    <tr><td valign="top"><code>--help</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Print help message
</td></tr>

<tr></tr>
    <tr id="cli-print-info"><td valign="top"><code>--print-info</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Print dump files statistics to stdout as json/yaml<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-format"><code>--format &lt;json|yaml&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-color"><code>--no-color</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-pretty"><code>--no-pretty</a></code>
</td></tr>

<tr></tr>
    <tr id="cli-print-requests"><td valign="top"><code>--print-requests</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Print dump requests to stdout as json/yaml<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-format"><code>--format &lt;json|yaml&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-color"><code>--no-color</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-pretty"><code>--no-pretty</a></code>
</td></tr>

<tr></tr>
    <tr id="cli-print-openapi"><td valign="top"><code>--print-openapi</code>
    </td>
    <td valign="top"></td>
    <td valign="top">
    Print OpenAPI specification to stdout as json/yaml<br>
    <br>See Also:<br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-openapi-path"><code>--openapi-path &lt;path&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-openapi-title"><code>--openapi-title &lt;text&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-format"><code>--format &lt;json|yaml&gt;</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-color"><code>--no-color</code></a><br>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-no-pretty"><code>--no-pretty</code></a>
	<img width="1000" height="0">
</td></tr>
</table>

<br><br>

<h2>
APPENDIX B.
<br>
Optional REQUEST / RESPONSE headers
</h2>
<table>
    <tr><th width="220rem">Header</th><th>Description</th></tr>
<tr></tr>
    <tr><td valign="top"><pre>X-Delay</pre>
	<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/request.svg"></a>
	<br><a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/response.svg"></a>
	</td>
    <td valign="top"><p>Request or response delay (in milliseconds).</p>
    <i>Examples:</i><br>
    <br>

Two seconds request delay:<br>
```http
GET / HTTP/1.1
X-Delay: 2000
```

<h2></h2>

Two seconds response delay:<br>
```http
HTTP/1.1 200 OK
X-Delay: 2000
```

</td></tr>
<tr></tr>
    <tr><td valign="top"><pre>X-Content-Source</pre>
	<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/request.svg"></a>
	<br><a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/response.svg"></a>
    </td>
    <td valign="top">
    The URL of the externally hosted content.<br>
    <br>
    The content from the URL will be sent as the response body.
    Supported protocols: <code>http:</code>, <code>https:</code>, <code>file:</code>, <code>data:</code>.<br>
    If the URL provides its own content type and there is no <code>Content-Type</code> header in the dump, the original <code>Content-Type</code> header received from the URL will be sent along with other response headers.
    <br>
    This header is useful when you want to send content hosted on a remote server or just send binary data as a response body.<br>
	<br>
    <i>Examples:</i>
<br><br>

Get a response body from a remote server:<br>

```http
HTTP/1.1 200 OK
Content-Type: application/json
X-Content-Source: http://example.com/api/car/1234.json
```

<h2></h2>

Get a response body from a file:<br>

```http
HTTP/1.1 200 OK
Content-Type: image/jpeg
X-Content-Source: file:///home/john/photo.jpeg
```

<h2></h2>

Get a response body from a data URI:<br>

```http
HTTP/1.1 200 OK
Content-Type: image/gif
X-Content-Source: data:image/gif;base64,R0lGODlhAQABAIAAAP...
```
</td></tr>
<tr></tr>
    <tr id="X-OpenAPI-Summary"><td valign="top"><pre>X-OpenAPI-Summary</pre>
	<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/request.svg"></a>
    </td>
    <td valign="top">
    <p>OpenAPI request summary text.</p>
    <i>Example:</i>

```http
GET /api/customer{id} HTTP/1.1
X-OpenAPI-Summary: Get customer information
```
<br>See Also:<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-OpenAPI-Description"><code>X-OpenAPI-Description</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-OpenAPI-Tags"><code>X-OpenAPI-Tags</code></a>

</td></tr>
<tr></tr>
    <tr id="X-OpenAPI-Description"><td valign="top"><pre>X-OpenAPI-Description</pre>
	<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/request.svg"></a>
    </td>
    <td valign="top">
    OpenAPI request description text.<br>
    <br>
    <i>Example:</i>

```http
GET /api/customer{id} HTTP/1.1
X-OpenAPI-Summary: Get customer information
X-OpenAPI-Description: This API extracts customer info from db
```
<br>See Also:<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-OpenAPI-Summary"><code>X-OpenAPI-Summary</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-OpenAPI-Tags"><code>X-OpenAPI-Tags</code></a>

</td></tr>
<tr></tr>
    <tr id="X-OpenAPI-Tags"><td valign="top"><pre>X-OpenAPI-Tags</pre>
	<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/request.svg"></a>
    </td>
    <td valign="top">
    OpenAPI request comma-separated tag list.<br>
    <br>
    <i>Example:</i>

```http
GET /api/customer{id} HTTP/1.1
X-OpenAPI-Summary: Get customer information
X-OpenAPI-Description: This API extracts customer info from db
X-OpenAPI-Tags: Work with customer, Buyers, Login info
```
<br>See Also:<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-OpenAPI-Summary"><code>X-OpenAPI-Summary</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-OpenAPI-Description"><code>X-OpenAPI-Description</code></a>

</td></tr>
<tr></tr>
        <tr><td valign="top"><pre>X-Forward-To</pre>
		<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/response.svg"></a>
        </td>
    <td valign="top">
    Forward client request to specified origin.<br>
    <br>
    Acts as a forward proxy.<br>
    <br>
    <i>Example:</i>
<br>

```http
HTTP/1.1
X-Forward-To: http://example.com:8080
```
</td></tr>
<tr></tr>
        <tr id="X-Handler-CGI"><td valign="top"><pre>X-Handler-CGI</pre>
		<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/response.svg"></a>
        </td>
    <td valign="top">
    <strong>CGI</strong> (<strong>C</strong>ommon <strong>G</strong>ateway <strong>I</strong>nterface) program.<br>
    <br>
    <i>Example:</i>
<br>

```http
HTTP/1.1 200 OK
X-Handler-CGI: /home/john/myprog.sh param1 param2
```
<br>See Also:<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-XGI"><code>X-Handler-XGI</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-JS"><code>X-Handler-JS</code></a>
</td></tr>
<tr></tr>
        <tr id="X-Handler-XGI"><td valign="top"><pre>X-Handler-XGI</pre>
		<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/response.svg"></a>
        </td>
    <td valign="top">
    <strong>XGI</strong> (<strong>E</strong>xtended <strong>G</strong>ateway <strong>I</strong>nterface) program.<br>
    <br>
XGI program is very similar to CGI, but unlike CGI, the XGI program 
reads from stdin not only the body of the request but also the first line and the headers.
In response XGI program writes <i>status line</i>, <i>headers</i> and <i>response body </i>into stdout.<br>
All CGI environment variables are also available to XGI program.<br>
    <br>
    <i>Example:</i>
<br>

```http
# NOTE:
# The actual status line will be generated by the XGI program.

HTTP/1.1
X-XGI: /home/john/myprog.sh param1 param2
```
<br>See Also:<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-CGI"><code>X-Handler-CGI</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-JS"><code>X-Handler-JS</code></a>
</td></tr>
<tr></tr>
        <tr id="X-Handler-JS"><td valign="top"><pre>X-Handler-JS</pre>
		<a href="#appendix-boptional-request--response-headers"><img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/img/response.svg"></a>
        </td>
    <td valign="top">
    JavaScript response handler function.<br>
    <br>
The JavaScript functions are taken from the context files listed in the
<br>
<a href="#cli-js"><code>--js &lt;file|url&gt;...</code></a> option.<br>
<br>
The following objects are provided as handler function parameters:<br>
<ul>
	<li>
	<strong><code>request</code></strong> - <i>object</i>, <strong>READ ONLY</strong>
	<ul>
		<li><code>request.method</code>: <i>string</i> E.g. <code>request.method</code> ➞ <code>post</code></li>
		<li><code>request.path</code>: <i>string</i> E.g. <code>request.path</code> ➞ <code>/api/customers</code></li>
		<li><code>request.query</code>: <i>string</i> E.g. <code>request.query</code> ➞ <code>fname=John&lname=Doe</code></li>
		<li><code>request.parameters</code>: <i>object</i> E.g. <code>request.parameters.fname[0]</code> ➞ <code>John</code></li>
		<li><code>headers</code>: <i>object</i> E.g. <code>request.headers['content-type'][0]</code> ➞ <code>application/json</code></li>
		<li><code>request.body</code>: <i>string | object</i> E.g. <code>request.body</code> ➞ <code>{"fname": "Jonh", lname: "Doe"}</code></li>
	</ul>
	</li>
	<li>
	<strong><code>response</code></strong> - <i>object</i>, <strong>READ | WRITE</strong>
	<ul>
		<li><code>response.status</code>: <i>number</i> E.g. <code>response.status = 200</code></li>
		<li><code>response.headers</code>: <i>object</i> E.g. <code>response.headers['Content-Type'] = 'application/json'</code></li>
		<li><code>response.body</code>: <i>string | object</i> E.g. <code>response.body = {"fname": "Jonh", lname: "Doe"}</code></li>
	</ul>
	</li>
	<li><strong><code>data</code></strong>: <i>object</i>, <strong>READ | WRITE</strong> - persistent user data from the file provided by <code>--db</code> option</li>
</ul>
Among other things, the <code>X-Handler-JS</code> header allows you to modify persistent data.<br>
<br>
    <i>Examples:</i><br>
<br>

JavaScript function modify memory data:

```http
DELETE /customers/{id}

HTTP/1.1 200 OK
X-Handler-JS: deleteCustomer
Content-Type: application/json

{"id": "${request.parameters.id[0]}"};

```

```js
function deleteCustomer(request, response, data) {
    const id = request.parameters.id[0];
    delete data.customers[id];
}
```
<h2></h2>
JavaScript function modify memory data and provide response status, headers and body:

```http
DELETE /customers/{id}

HTTP/1.1
X-Handler-JS: deleteCustomer

```

```js
function deleteCustomer(request, response, data) {
    const id = request.parameters.id[0];
    if (data.customers[id] === undefined) {
        response.status = 404;
        response.body = {error: true, message: 'ID not found'};
    }
    delete data.customers[id];
    response.status = 200;
    response.headers['Content-Type'] = 'application/json';
    response.body = {error: false, message: null};
}
```

<br>See Also:<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-js"><code>--js &lt;file|url&gt;...</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#cli-db"><code>--db &lt;file|url&gt;...</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-CGI"><code>X-Handler-CGI</code></a><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#X-Handler-XGI"><code>X-Handler-XGI</code></a>
<img width="1000" height="0">
</td></tr>
</table>
<br>

>**NOTE:**<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1. Optional request headers will **not** be sent to the server engine.<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2. Optional response headers will **not** be sent to clients.
