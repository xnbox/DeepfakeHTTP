<h1>REST version of the Spring PetClinic Sample Application built with DeepfakeHTTP</h1>
<p id="start" align="center">
<br>
<a href="#start"><img width="250rem" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/pets.png"></a>
<h1></h1>
</p>

<br>
<p id="banner" align="center">
<br>
<table>
<tr>
<td>
<br><a href="#banner"><img align="left" src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/1.png" width="190"></a>
<h3>About Spring Petclinic</h3>
The Spring PetClinic is an example application designed to show how a particular stack can be used to create simple but powerful database-oriented applications.<br>
<img width="1000" height="0">
</td>
</tr>
</table>
</p>
<h2>Get started</h2>

<ol>
    <li>Download the <a href="https://github.com/xnbox/DeepfakeHTTP/releases/latest">latest releases</a> of <code>df.jar</code> and <code>df-spring-petclinic.zip</code></li>
    <li>Unzip the <code>df-spring-petclinic.zip</code> archive</li>
<li>Start the server from command line:

```
java -jar df.jar \
--dump PetClinic/petclinic.txt \
--js PetClinic/context.js \
--dir PetClinic \
--openapi-title 'Spring Pet Clinic API v1.2.7' \
--db PetClinic/petclinic.json \
--openapi-path /api \
--db-path /db
```
</li>

<li>Use a browser to check whether the server is running:
<br>
<pre><a href="http://localhost:8080">http://localhost:8080</a></pre>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/1.png">
</li>
</li>

<li>OpenAPI client:
<br>
<pre><a href="http://localhost:8080/api">http://localhost:8080/api</a></pre>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/2.png">
</li>

<li>openapi.json:
<br>
<pre><a href="http://localhost:8080/api/openapi.json">http://localhost:8080/api/openapi.json</a></pre>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/3.png">
</li>

<li>openapi.yaml:
<br>
<pre><a href="http://localhost:8080/api/openapi.yaml">http://localhost:8080/api/openapi.yaml</a></pre>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/4.png">
</li>


<li>data:
<br>
<pre><a href="http://localhost:8080/db">http://localhost:8080/db</a></pre>
<img src="https://raw.githubusercontent.com/xnbox/DeepfakeHTTP/main/PetClinic/README/petclinic-screenshots/5.png">
</li>

</ol>