Java ver 17
com.travelagency
to do:
carpetas
ver tema de optimistic locking para evitar Cond de carrera(?
)
RestTemplate es una clase que se utiliza para hacer peticiones HTTP; es decir, es la herramienta que este microservicio (ms-bundle)
 usará para comunicarse con otros microservicios de tu ecosistema.

-----------------------------------------------
EUREKA SERVER = http://localhost:8761



-----------------------------------------------




# ¿Qué es y para qué sirve Eureka?
Es el "Directorio Telefónico" (Service Registry) de los microservicios. Sirve para que el API Gateway y los demás microservicios se encuentren entre sí usando solo sus nombres, sin depender de IPs fijas. Su panel de control está en http://localhost:8761, donde puedes ver qué servicios están registrados.
- Service Discovery, Balanceo de carga y api Gateway, desacoplamiento { es para lo de minikube}
# ¿Cómo correr Eureka?
En una terminal PowerShell, ve a la carpeta del servidor y ejecútalo con Maven:
cd C:\Users\diego\Documents\GitHub\TravelAgency-Tingeso2\Backend-microservicios\eureka-server
.\mvnw.cmd spring-boot:run
para pauarlo usa ctrl c