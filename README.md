# Proyecto final Curso Kafka USTA

## _Sistema de generación y procesamiento de eventos sobre una aplicación Web basado en Kafka_

Sistema de generación y persistencia de eventos en tiempo real sobre una aplicación Web basado en Kafka, se utilizan los siguientes componentes:

- Componente Kafka, para procesar los eventos
- Componente generador de eventos basado en Java (producer)
- Componente consumidor de los eventos basado en Java (consumer)
- Base de datos relacional Postgres

## Componente Kafka

Contenedores basado en Podman para alojar los sistemas Kafka y Zookeeper que son los encargados de procesar los eventos generados por el productor y remitirlos al consumidor de forma instantánea.

También se incluye la definición de un contenedor para alojar una instancia de Kafdrop, para hacer temas de monitoreo básico sobre Kafka.

A continuación ser muestra la sección de configuración en el archivo `podman-compose.yml`:

```bash
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0   # Imagen oficial de Confluent para Zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"                          # Puerto de Zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181            # Puerto por defecto de clientes
      ZOOKEEPER_TICK_TIME: 2000              # Tiempo base de sincronización

  kafka:
    image: confluentinc/cp-kafka:7.5.0       # Imagen oficial de Kafka
    container_name: kafka
    ports:
      - "9092:9092"                          # Puerto externo para clientes
      - "29092:29092"                        # Puerto interno (para otros contenedores)
    environment:
      KAFKA_BROKER_ID: 1                     # Identificador único del broker
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181 # Dirección de Zookeeper
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://172.24.186.249:9092,PLAINTEXT_INTERNAL://kafka:29092
      # PLAINTEXT -> conexión externa desde tu máquina
      # PLAINTEXT_INTERNAL -> conexión interna entre contenedores
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1  # Número de réplicas mínimas (1 en pruebas)
      KAFKA_CREATE_TOPICS: "eventos-pagina:1:1"  # Creación de tópico eventos-pagina topic_name:partitions:replication_factor
    depends_on:
      - zookeeper   # Kafka necesita que Zookeeper esté arriba primero

  kafdrop:
    image: obsidiandynamics/kafdrop:latest #Imagen Base de kafdrop, interfaz grafica de kafka
    container_name: kafdrop
    ports:
      - "9000:9000"   # Abres en el navegador: http://localhost:9000
    environment:
      KAFKA_BROKERCONNECT: "kafka:29092"
    depends_on:
      - kafka
```

## Componente generador de eventos

Componente Web basado en Java y Springboot que aloja una página Web accesible a través de la URL http://localhost:8080/productor-eventos/web/home/enviar-evento, dicha web es una interfaz sencilla para simular la generación de eventos basados en la interacción del usuario, eventos que se envían a Kafka para su procesamiento.

También se expone un servicio REST en la URL http://localhost:8080/productor-eventos/api/v1/eventos/enviar como mecanismo alterno de reporte de eventos a Kafka.

A continuación se muestra el archivo de configuración del componente:

```
# Parametros generales
spring.application.name=generador-eventos
server.servlet.context-path=/productor-eventos
server.port=8080

# Configuración Kafka
spring.kafka.bootstrap-servers=172.24.186.249:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
```

El repositorio de código fuente se encuentra en la carpeta `generador-eventos` del repositorio.

AngularJS-powered HTML5 Markdown editor.

## Componente consumidor de eventos

Componente basado en Java y Springboot que define un consumidor de eventos basados en Kafka, dichos eventos se almacenan en una base de datos relacional sencilla alojada en Postgres, específicamente en la tabla `eventos_kafka`.

A continuación se muestra el archivo de configuración del componente:

```
# Parámetros generales
spring.application.name=consumidor-eventos
server.servlet.context-path=/consumidor-eventos
server.port=8090

# Configuración JPA
spring.datasource.url=jdbc:postgresql://172.24.186.249:5432/base_datos
spring.datasource.username=usuario_bd
spring.datasource.password=contrasenia_BD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Configuración Kafka
spring.kafka.bootstrap-servers=172.24.186.249:9092
spring.kafka.consumer.group-id=consumidor-eventos
```

El repositorio de código fuente se encuentra en la carpeta `consumidor-eventos` del repositorio.

## Base de datos relacional

Componente de base de datos basado en Postgres y definido como un contenedor utilizando Podman, utiliza un volumen de datos externo para que este no se vea afectado en los procesos de parada y arranque de las instancias.

A continuación se muestra las secciones relevantes de configuración en el archivo `podman-compose.yml`, donde se definen entre otros, los datos de acceso a la base de datos utilizada:

```
  postgres:
    image: postgres:16-alpine
    container_name: postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: usuario_bd
      POSTGRES_PASSWORD: contrasenia_BD
      POSTGRES_DB: base_datos
    volumes:
      - pg_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  pg_data:
```

## Configuración

Para la configuración se decidió construir los contenedores a través de WSL en Windows 11, utilizando la distribución ubuntu, para lo cual, estando en la consola se debe lanzar el siguiente comando para extraer la dirección IP asignada a la instancia en WSL, desde donde se van a acceder a los servicios alojados.

```bash
ip addr show eth0 | grep 'inet ' | awk '{print $2}' | cut -d/ -f1
```

Para el caso del montaje, esta dirección resultó ser 172.24.186.249.

Adicionalmente se debe tener en cuenta como prerrequisito, la configuración de podman en la instancia virtual de Ubuntu y opcionalmente un cliente de postgres, a través de estos comandos:

```bash
sudo apt update
sudo apt install -y podman
sudo apt install -y podman-compose
sudo apt install postgresql-client-common
sudo apt install postgresql-client-16
```

Adicionalmente se recomienda la adición de las siguientes líneas en el archivo /etc/containers/registries.conf:
```bash
[registries.search]
registries = ['docker.io', 'registry.access.redhat.com', 'registry.fedoraproject.org']
```

Se puede verificar a través de los siguientes comandos:

```bash
podman --version
podman-compose --version
podman run --rm hello-world
```

### 1. Copiar el repositorio de Github https://github.com/hlbarragan/proyecto-final.git

```bash
cd /mnt/d/CursoKafka/proyecto-final
git clone https://github.com/hlbarragan/proyecto-final.git
```

### 2. Levantar los contenedores de Podman

Se utiliza como referencia el archivo de configuración `podman-compose.yml`:

```
version: "3.8"

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0   # Imagen oficial de Confluent para Zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"                          # Puerto de Zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181            # Puerto por defecto de clientes
      ZOOKEEPER_TICK_TIME: 2000              # Tiempo base de sincronización

  kafka:
    image: confluentinc/cp-kafka:7.5.0       # Imagen oficial de Kafka
    container_name: kafka
    ports:
      - "9092:9092"                          # Puerto externo para clientes
      - "29092:29092"                        # Puerto interno (para otros contenedores)
    environment:
      KAFKA_BROKER_ID: 1                     # Identificador único del broker
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181 # Dirección de Zookeeper
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://172.24.186.249:9092,PLAINTEXT_INTERNAL://kafka:29092
      # PLAINTEXT -> conexión externa desde tu máquina
      # PLAINTEXT_INTERNAL -> conexión interna entre contenedores
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1  # Número de réplicas mínimas (1 en pruebas)
      KAFKA_CREATE_TOPICS: "eventos-pagina:1:1"  # Creación de tópico eventos-pagina topic_name:partitions:replication_factor
    depends_on:
      - zookeeper   # Kafka necesita que Zookeeper esté arriba primero

  kafdrop:
    image: obsidiandynamics/kafdrop:latest #Imagen Base de kafdrop, interfaz grafica de kafka
    container_name: kafdrop
    ports:
      - "9000:9000"   # Abres en el navegador: http://localhost:9000
    environment:
      KAFKA_BROKERCONNECT: "kafka:29092"
    depends_on:
      - kafka

  postgres:
    image: postgres:16-alpine
    container_name: postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_USER: usuario_bd
      POSTGRES_PASSWORD: contrasenia_BD
      POSTGRES_DB: base_datos
    volumes:
      - pg_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  pg_data:
```

Estando ubicado en el directorio donde está el archivo de configuración, se lanza el siguiente comando para construir y ejecutar los contenedores:

```bash
podman-compose up --build -d
```

Se puede verificar el resultado con el siguiente comando:

```bash
podman ps
```

Obteniendo la siguiente salida, donde se evidencia el levantamiento de las instancias zookeeper, postgres, kafka y kafdrop:

```
hbarraganb@000-3466XRN:~$ podman ps
CONTAINER ID  IMAGE                                      COMMAND               CREATED       STATUS            PORTS                                             NAMES
2435a207fd29  docker.io/confluentinc/cp-zookeeper:7.5.0  /etc/confluent/do...  22 hours ago  Up About an hour  0.0.0.0:2181->2181/tcp                            zookeeper
c64ea43c47b6  docker.io/library/postgres:16-alpine       postgres              22 hours ago  Up About an hour  0.0.0.0:5432->5432/tcp                            postgres
eebcce7d7579  docker.io/confluentinc/cp-kafka:7.5.0      /etc/confluent/do...  22 hours ago  Up About an hour  0.0.0.0:9092->9092/tcp, 0.0.0.0:29092->29092/tcp  kafka
489f305545c0  docker.io/obsidiandynamics/kafdrop:latest                        22 hours ago  Up About an hour  0.0.0.0:9000->9000/tcp                            kafdrop
```

### 3. Configurar el tópico de Kafka a utilizar

Se configura el tópico 'eventos-pagina', utilizado para la gestión de los eventos generados por el producer con el siguiente comando:

```bash
kafka-topics --create --topic eventos-pagina   --bootstrap-server localhost:9092   --partitions 1   --replication-factor 1
```

El registro del tópico se puede verificar con el siguiente comando:

```bash
kafka-topics --bootstrap-server localhost:9092 --list
```

La respuesta esperada es la siguiente:

```
[appuser@eebcce7d7579 ~]$ kafka-topics --bootstrap-server localhost:9092 --list
__consumer_offsets
eventos-pagina
```

También se puede verificar gráficamente en la URL de Kafdrop: http://localhost:9000/

### 4. Verificar la configuiración de la base de datos

Se recomienda verificar la conexión a la base de datos a través de la ejecución del siguiente comando, utilizando el cliente de postgres previamente configurado:

```bash
psql -h 172.24.186.249 -U usuario_bd -d base_datos
```

Un ejemplo de verificación exitosa es el siguiente:

```
hbarraganb@000-3466XRN:~$ psql -h 172.24.186.249 -U usuario_bd -d base_datos
Password for user usuario_bd:
psql (16.10 (Ubuntu 16.10-0ubuntu0.24.04.1))
Type "help" for help.

base_datos=#
```

### 5. Despliegue de los componentes productor y consumidor

- El código fuente del componente Web productor se encuentra en la carpeta `generador-eventos`, se recomienda utilizar vscode para importarlo y ejecutarlo sobre un entorno Java JRE 17.
- El código fuente del componente consumidor se encuewntra en la carpeta `consumidor-eventos`, se recomienda utilizar vscode para importarlo y ejecutarlo sobre una entorno Java JRE 17.

### 6. Verificación de funcionamiento

Una vez configurado los dos componentes basados en Java, se puede acceder a la URL http://localhost:8080/productor-eventos/web/home/, donde se generarán 3 tipos de eventos producto de la interacción del usuario con 3 botones.

Los eventos generados y enviados a Kafka se pueden monitorear por consola de Kafka, a través de los siguientes comandos:

```bash
podman exec -it kafka bash
kafka-console-consumer --topic eventos-pagina --bootstrap-server localhost:9092   --from-beginning
```

Estos también se pueden monitorear a través de Kafdrop en la URL http://localhost:9000/

Por último, se puede verificar la inserción de los eventos generados en la base de datos con cualquier cliente de preferencia, teniendo en cuenta los datos de conexión:
- hostname: 172.24.186.249
- Puerto: 5432
- Base de datos: base_datos
- Usuario: usuario_bd
- Contraseña: contrasenia_BD

La sentencia SQL para consultar los datos se muestra a continuación:\

```bash
SELECT * FROM public.eventos_kafka;
```

## ¡¡¡Gracias!!!

**Free Software, Hell Yeah!**
