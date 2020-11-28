# Primera Parte

[![Build Status](https://travis-ci.org/FdelMazo/7527-AlgoritmosIV.svg?branch=main)](https://travis-ci.org/FdelMazo/7527-AlgoritmosIV)

La primera instancia consta de un desarrollo en el lenguaje de programación Scala haciendo uso exclusivamente de herramientas del paradigma funcional y comentando el mismo utilizando el estilo Scaladoc.
Es decir que el código producido deberá ser Funcional Puro.

Construir un pipeline de datos de manera puramente funcional para llenar una base de datos a partir del csv train.csv provisto.

Se debe mapear las filas del archivo a objetos ```DataSetRow``` validando su contenido segun los constrains de la base de datos.
```sql
CREATE TABLE fptp.dataset
(
    id int PRIMARY KEY,
    date timestamp without time zone NOT NULL,
    open double precision,
    high double precision,
    low double precision,
    last double precision not null,
    close double precision not null,
    dif double precision not null,
    curr character varying(1) NOT NULL,
    o_vol int,
    o_dif int,
    op_vol int,
    unit character varying(4) NOT NULL,
    dollar_BN double precision NOT NULL,
    dollar_itau double precision not null,
    w_diff double precision not null,
    hash_code int not null
);
```

Si un campo nulleable tiene error se guarda como null, sino se descarta la linea.

# Segunda Parte

La segunda parte consiste en crear un modelo de ML a partir de nuestra data en la db, para esto sera necesario hacer un programa que levante la data de la db (en doobie), convierta los valores a numeros, parta la data en train y test (70/30). Para finalmente pasarsela a un pipeline de spark que nos cree un modelo Rando Forest regressor y sea persistido en formato [PMML](http://dmg.org/pmml/v4-4/GeneralStructure.html).

## Instalación

Instalar *sbt* (instrucciones [aquí](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html))

Compilar con `sbt compile`.

Ejecutar con `sbt "run train.csv <host>"`.

### Testing

Ejecutar tests con `sbt test`.

# Correr tests con podman/docker
Usando `docker-compose`/`podman-compose`:

Desde el root del repo:
```bash
podman-compose -f docker/docker-compose.yaml up --abort-on-container-exit
```
