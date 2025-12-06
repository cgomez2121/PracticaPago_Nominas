# PracticaPago_Nominas

Este proyecto implementa un sistema de pago de nóminas concurrente siguiendo el patrón productor/consumidor. La arquitectura se basa en tres procesos principales: el padre (ApPrinc) que lanza el flujo, el generador (SubApGenFT) que crea el archivo de transferencias (usando un protocolo de bloqueo y simulando fallos aleatorios), y el procesador (SubApProcFT) que realiza una espera activa (polling), carga los datos y utiliza un pool de hilos para el procesamiento concurrente. La seguridad del saldo y la acumulación de resultados se gestionan mediante Monitores (GestorSaldo, GestorResultados), asegurando la exclusión mutua y previniendo condiciones de carrera.




Autor: Carlos José Gómez Sánchez
Curso: DA2D1A 
Asignatura: Programación de servicios y procesos
