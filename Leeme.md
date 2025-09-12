Lo que investigué de la issue pq no entendía nada:


Crear una clase o funciones que manejen todo lo relacionado con SQLite: abrir conexión, ejecutar consultas (SELECT, INSERT, UPDATE, DELETE), y cerrar la conexión.

Usar el patrón Singleton, que asegura que solo exista una conexión a la base de datos en toda la aplicación, evitando múltiples conexiones innecesarias.


Ventajas del Singleton acá:

Si en varias partes del programa usás SQLiteConnection("mi_base.db"), siempre devuelve la misma conexión, no una nueva.

Evitás bloqueos o errores por abrir muchas conexiones simultáneamente.

---------------------------------------------------------
Y POO pq no me acordaba 


Los Cuatro Principios de la POO

    Encapsulación:
    Consiste en agrupar datos (atributos) y los métodos (comportamientos) que operan sobre ellos dentro de una misma unidad, como una clase. Esto protege la integridad de los datos, impidiendo modificaciones no válidas y proporcionando un punto de acceso controlado. 

Herencia:
Permite que una clase (clase hija o secundaria) pueda heredar las propiedades y comportamientos de otra clase (clase padre o principal). Esto fomenta la reutilización de código, ya que no es necesario escribir la misma lógica repetidamente. 
Polimorfismo:
Significa "muchas formas". Se refiere a la capacidad de diferentes objetos de responder al mismo mensaje (llamada a método) de manera específica a su tipo. Esto aporta flexibilidad y permite tratar objetos de manera genérica a través de una interfaz común. 
Abstracción:
Es el proceso de representar las características esenciales de un objeto y ocultar los detalles internos innecesarios. Se centra en lo que el objeto hace en lugar de cómo lo hace, exponiendo solo los métodos públicos de alto nivel para acceder a un objeto. 

Beneficios de estas "Reglas"

    Reutilización de código:
    Gracias a la herencia y la abstracción, se puede crear software más eficiente y rápido de desarrollar. 

Flexibilidad y Mantenimiento:
La encapsulación y el polimorfismo facilitan la actualización y el mantenimiento de partes del sistema sin afectar significativamente el resto del código. 
Modularidad:
La agrupación de datos y métodos en objetos permite crear componentes independientes y bien definidos. 
Seguridad:
La encapsulación protege la integridad de los datos al controlar el acceso a los mismos. 

