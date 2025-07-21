# Spring Framework 6: Beginner to Guru
## Spring Data Rest

## Swagger/Openapi Url

- http://localhost:8084/swagger-ui/index.html
- http://localhost:30084/swagger-ui/index.html

## Web Interface

This application includes a web interface that allows users to interact with the beer data through a browser. The web interface provides the following features:

- View a paginated list of beers
- Navigate through pages of beer listings
- View details of individual beers

To access the web interface, start the application and navigate to: 

- http://localhost:8084/web/beers
- http://localhost:30084/web/beers

## Kubernetes

To run maven filtering for destination target/k8s and destination target/helm run:
```bash
mvn clean install -DskipTests 
```

### Deployment with Kubernetes

Deployment goes into the default namespace.

To deploy all resources:
```bash
kubectl apply -f target/k8s/
```

To remove all resources:
```bash
kubectl delete -f target/k8s/
```

Check
```bash
kubectl get deployments -o wide
kubectl get pods -o wide
```

You can use the actuator rest call to verify via port 30084

### Deployment with Helm

Be aware that we are using a different namespace here (not default).

Go to the directory where the tgz file has been created after 'mvn install'
```powershell
cd target/helm/repo
```

unpack
```powershell
$file = Get-ChildItem -Filter spring-6-data-rest-v*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install
```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace spring-6-data-rest --create-namespace --wait --timeout 5m --debug --render-subchart-notes
```

show logs
```powershell
kubectl get pods -n spring-6-data-rest
```

replace $POD with pods from the command above
```powershell
kubectl logs $POD -n spring-6-data-rest --all-containers
```

Show Endpoints
```powershell
kubectl get endpoints -n sdjpa-intro
```

status
```powershell
helm status $APPLICATION_NAME --namespace spring-6-data-rest
```

test
```powershell
helm test $APPLICATION_NAME --namespace spring-6-data-rest --logs
```

uninstall
```powershell
helm uninstall $APPLICATION_NAME  --namespace spring-6-data-rest
```

delete all
```powershell
kubectl delete all --all -n spring-6-data-rest
```

create busybox sidecar
```powershell
kubectl run busybox-test --rm -it --image=busybox:1.36 --namespace=spring-6-data-rest --command -- sh
```