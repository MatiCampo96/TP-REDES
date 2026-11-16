BUILD (desde la carpeta raiz del proyecto)
docker build -t tp-redes .

RUN (en segundo plano)
docker run -d --rm -p 8080:8080 --name tp-redes tp-redes

ENTRAR al contenedor
docker exec -it tp-redes sh

DETENER
docker stop tp-redes

O en lugar de Docker se puede correr con Java 17 y Maven 3.9
En el directorio raiz del proyecto: mvn spring-boot:run

GET: pagina principal
curl.exe -i http://localhost:8080/

GET: parametros
curl.exe -i "http://localhost:8080/?nombre=Ana&edad=20"

GET: archivo estatico
curl.exe -i http://localhost:8080/styles.css

GET: probar 404
curl.exe -i http://localhost:8080/archivo-inexistente.html

POST: formulario
curl.exe -i -X POST http://localhost:8080/ `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data "nombre=Ana&edad=20"
